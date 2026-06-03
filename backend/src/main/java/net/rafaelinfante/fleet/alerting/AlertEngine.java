package net.rafaelinfante.fleet.alerting;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.rafaelinfante.fleet.domain.AlertRule;
import net.rafaelinfante.fleet.domain.AlertRuleRepository;
import net.rafaelinfante.fleet.domain.AlertType;
import net.rafaelinfante.fleet.domain.DeviceStatus;
import net.rafaelinfante.fleet.domain.DeviceType;
import net.rafaelinfante.fleet.domain.Geofence;
import net.rafaelinfante.fleet.domain.GeofenceMode;
import net.rafaelinfante.fleet.domain.Metric;
import net.rafaelinfante.fleet.messaging.TelemetryEvent;

/**
 * Stateless rule evaluation. For each reading every enabled rule is checked and the matching
 * alert is raised or resolved; because at most one ACTIVE alert exists per (device, rule), this
 * gives edge-triggered behaviour (raised when the condition first holds, resolved when it clears)
 * without tracking previous state.
 */
@Service
public class AlertEngine {

    private final AlertRuleRepository ruleRepository;
    private final AlertService alertService;

    public AlertEngine(AlertRuleRepository ruleRepository, AlertService alertService) {
        this.ruleRepository = ruleRepository;
        this.alertService = alertService;
    }

    @Transactional
    public void evaluateReading(TelemetryEvent event) {
        // A reading proves the device is alive, so clear any standing offline alert.
        alertService.resolveOffline(event.deviceId());
        for (AlertRule rule : ruleRepository.findByEnabledTrue()) {
            if (!appliesTo(rule, event.deviceType())) {
                continue;
            }
            switch (rule.getType()) {
                case THRESHOLD -> evaluateThreshold(event, rule);
                case GEOFENCE -> evaluateGeofence(event, rule);
                case OFFLINE -> { /* driven by status transitions and the offline job */ }
            }
        }
    }

    @Transactional
    public void evaluateStatus(TelemetryEvent event) {
        if (event.status() == DeviceStatus.OFFLINE) {
            // A last-will carries no device type, so only globally-scoped offline rules apply here;
            // type-scoped offline rules are matched by the offline-detection job, which knows the type.
            offlineRuleFor(ruleRepository.findByEnabledTrue(), event.deviceType()).ifPresent(rule ->
                    alertService.raiseOffline(event.deviceId(), rule, "Device went offline (last-will received)"));
        } else if (event.status() == DeviceStatus.ONLINE) {
            alertService.resolveOffline(event.deviceId());
        }
    }

    public Optional<AlertRule> offlineRuleFor(List<AlertRule> enabledRules, DeviceType deviceType) {
        return enabledRules.stream()
                .filter(rule -> rule.getType() == AlertType.OFFLINE)
                .filter(rule -> appliesTo(rule, deviceType))
                .findFirst();
    }

    public List<AlertRule> enabledRules() {
        return ruleRepository.findByEnabledTrue();
    }

    private void evaluateThreshold(TelemetryEvent event, AlertRule rule) {
        Double value = metricValue(event, rule.getMetric());
        if (value == null || rule.getOperator() == null || rule.getThreshold() == null) {
            return;
        }
        if (rule.getOperator().test(value, rule.getThreshold())) {
            String message = "%s (observed %s)".formatted(rule.getName(), formatMetric(rule.getMetric(), value));
            alertService.raiseForRule(event.deviceId(), rule, value, message);
        } else {
            alertService.resolveForRule(event.deviceId(), rule.getId());
        }
    }

    private void evaluateGeofence(TelemetryEvent event, AlertRule rule) {
        Geofence fence = rule.getGeofence();
        if (fence == null || rule.getGeofenceMode() == null
                || event.latitude() == null || event.longitude() == null) {
            return;
        }
        double distance = GeoUtils.haversineMeters(
                event.latitude(), event.longitude(), fence.getCenterLat(), fence.getCenterLng());
        boolean inside = distance <= fence.getRadiusM();
        boolean breach = (rule.getGeofenceMode() == GeofenceMode.ENTER && inside)
                || (rule.getGeofenceMode() == GeofenceMode.EXIT && !inside);
        if (breach) {
            String message = "%s (%.1f km from '%s')".formatted(
                    rule.getName(), distance / 1000.0, fence.getName());
            alertService.raiseForRule(event.deviceId(), rule, distance, message);
        } else {
            alertService.resolveForRule(event.deviceId(), rule.getId());
        }
    }

    private static boolean appliesTo(AlertRule rule, DeviceType type) {
        if (rule.getDeviceType() == null) {
            return true;
        }
        return rule.getDeviceType() == type;
    }

    private static Double metricValue(TelemetryEvent event, Metric metric) {
        if (metric == null) {
            return null;
        }
        return switch (metric) {
            case SPEED -> event.speedKmh();
            case BATTERY -> event.batteryPct();
            case FUEL -> event.fuelPct();
            case ENGINE_TEMP -> event.engineTempC();
        };
    }

    private static String formatMetric(Metric metric, double value) {
        String unit = switch (metric) {
            case SPEED -> "km/h";
            case BATTERY, FUEL -> "%";
            case ENGINE_TEMP -> "°C";
        };
        return String.format(Locale.ROOT, "%.1f %s", value, unit);
    }
}
