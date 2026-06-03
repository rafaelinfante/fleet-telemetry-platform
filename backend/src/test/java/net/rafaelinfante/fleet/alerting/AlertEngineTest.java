package net.rafaelinfante.fleet.alerting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.rafaelinfante.fleet.domain.AlertRule;
import net.rafaelinfante.fleet.domain.AlertRuleRepository;
import net.rafaelinfante.fleet.domain.AlertSeverity;
import net.rafaelinfante.fleet.domain.AlertType;
import net.rafaelinfante.fleet.domain.ComparisonOperator;
import net.rafaelinfante.fleet.domain.DeviceStatus;
import net.rafaelinfante.fleet.domain.DeviceType;
import net.rafaelinfante.fleet.domain.Geofence;
import net.rafaelinfante.fleet.domain.GeofenceMode;
import net.rafaelinfante.fleet.domain.Metric;
import net.rafaelinfante.fleet.messaging.TelemetryEvent;

@ExtendWith(MockitoExtension.class)
class AlertEngineTest {

    @Mock
    private AlertRuleRepository ruleRepository;

    @Mock
    private AlertService alertService;

    @InjectMocks
    private AlertEngine engine;

    private static TelemetryEvent readingAt(double lat, double lng, double speedKmh) {
        return TelemetryEvent.reading("VH-1", DeviceType.VAN, Instant.now(), lat, lng, speedKmh, 80.0, 70.0, 90.0, 1.0);
    }

    @Test
    void thresholdBreachRaisesAlert() {
        AlertRule rule = thresholdRule(120);
        when(ruleRepository.findByEnabledTrue()).thenReturn(List.of(rule));

        engine.evaluateReading(readingAt(53.3, -6.2, 150));

        verify(alertService).raiseForRule(eq("VH-1"), eq(rule), eq(150.0), anyString());
    }

    @Test
    void thresholdWithinLimitResolvesAlert() {
        AlertRule rule = thresholdRule(120);
        when(ruleRepository.findByEnabledTrue()).thenReturn(List.of(rule));

        engine.evaluateReading(readingAt(53.3, -6.2, 80));

        verify(alertService).resolveForRule("VH-1", rule.getId());
    }

    @Test
    void leavingGeofenceRaisesAlert() {
        AlertRule rule = geofenceRule(GeofenceMode.EXIT);
        when(ruleRepository.findByEnabledTrue()).thenReturn(List.of(rule));

        engine.evaluateReading(readingAt(53.50, -6.20, 50)); // ~17 km from the fence centre

        verify(alertService).raiseForRule(eq("VH-1"), eq(rule), org.mockito.ArgumentMatchers.anyDouble(), anyString());
    }

    @Test
    void insideGeofenceResolvesExitAlert() {
        AlertRule rule = geofenceRule(GeofenceMode.EXIT);
        when(ruleRepository.findByEnabledTrue()).thenReturn(List.of(rule));

        engine.evaluateReading(readingAt(53.3498, -6.2603, 50)); // at the centre

        verify(alertService).resolveForRule("VH-1", rule.getId());
    }

    @Test
    void offlineStatusRaisesAndOnlineResolves() {
        AlertRule offline = new AlertRule();
        offline.setId(9L);
        offline.setType(AlertType.OFFLINE);
        offline.setSeverity(AlertSeverity.WARNING);
        offline.setEnabled(true);
        when(ruleRepository.findByEnabledTrue()).thenReturn(List.of(offline));

        engine.evaluateStatus(TelemetryEvent.status("VH-1", Instant.now(), DeviceStatus.OFFLINE));
        verify(alertService).raiseOffline(eq("VH-1"), eq(offline), anyString());

        engine.evaluateStatus(TelemetryEvent.status("VH-1", Instant.now(), DeviceStatus.ONLINE));
        verify(alertService).resolveOffline("VH-1");
    }

    @Test
    void offlineRuleRespectsDeviceType() {
        AlertRule truckOnly = new AlertRule();
        truckOnly.setId(7L);
        truckOnly.setType(AlertType.OFFLINE);
        truckOnly.setSeverity(AlertSeverity.WARNING);
        truckOnly.setEnabled(true);
        truckOnly.setDeviceType(DeviceType.TRUCK);
        var rules = java.util.List.of(truckOnly);

        assertThat(engine.offlineRuleFor(rules, DeviceType.TRUCK)).contains(truckOnly);
        assertThat(engine.offlineRuleFor(rules, DeviceType.CAR)).isEmpty();
    }

    private static AlertRule thresholdRule(double threshold) {
        AlertRule rule = new AlertRule();
        rule.setId(1L);
        rule.setName("Overspeed");
        rule.setType(AlertType.THRESHOLD);
        rule.setMetric(Metric.SPEED);
        rule.setOperator(ComparisonOperator.GT);
        rule.setThreshold(threshold);
        rule.setSeverity(AlertSeverity.WARNING);
        rule.setEnabled(true);
        return rule;
    }

    private static AlertRule geofenceRule(GeofenceMode mode) {
        Geofence fence = new Geofence();
        fence.setId(5L);
        fence.setName("Operating area");
        fence.setCenterLat(53.3498);
        fence.setCenterLng(-6.2603);
        fence.setRadiusM(2000);
        AlertRule rule = new AlertRule();
        rule.setId(2L);
        rule.setName("Geofence");
        rule.setType(AlertType.GEOFENCE);
        rule.setGeofence(fence);
        rule.setGeofenceMode(mode);
        rule.setSeverity(AlertSeverity.WARNING);
        rule.setEnabled(true);
        return rule;
    }
}
