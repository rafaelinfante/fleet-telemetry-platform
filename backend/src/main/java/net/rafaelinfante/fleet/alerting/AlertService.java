package net.rafaelinfante.fleet.alerting;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.rafaelinfante.fleet.domain.Alert;
import net.rafaelinfante.fleet.domain.AlertRepository;
import net.rafaelinfante.fleet.domain.AlertRule;
import net.rafaelinfante.fleet.domain.AlertStatus;
import net.rafaelinfante.fleet.domain.AlertType;
import net.rafaelinfante.fleet.observability.FleetMetrics;
import net.rafaelinfante.fleet.web.TelemetryBroadcaster;
import net.rafaelinfante.fleet.web.dto.AlertView;
import net.rafaelinfante.fleet.web.dto.PageResponse;
import net.rafaelinfante.fleet.web.error.ResourceNotFoundException;
import net.rafaelinfante.fleet.web.mapper.AlertMapper;

/**
 * Creates, resolves and acknowledges alerts while keeping at most one ACTIVE alert per
 * (device, rule) — or per (device, OFFLINE). Alert evaluation runs on a single consumer thread,
 * so these checks need no extra locking.
 */
@Service
public class AlertService {

    // An alert is "open" until resolved; an acknowledged alert still suppresses duplicates and auto-resolves.
    private static final List<AlertStatus> OPEN = List.of(AlertStatus.ACTIVE, AlertStatus.ACKNOWLEDGED);

    private final AlertRepository repository;
    private final FleetMetrics metrics;
    private final TelemetryBroadcaster broadcaster;
    private final AlertMapper mapper;

    public AlertService(AlertRepository repository, FleetMetrics metrics,
                        TelemetryBroadcaster broadcaster, AlertMapper mapper) {
        this.repository = repository;
        this.metrics = metrics;
        this.broadcaster = broadcaster;
        this.mapper = mapper;
    }

    @Transactional
    public void raiseForRule(String deviceId, AlertRule rule, Double observedValue, String message) {
        if (repository.findFirstByDeviceIdAndRuleIdAndStatusIn(deviceId, rule.getId(), OPEN).isPresent()) {
            return;
        }
        Alert alert = new Alert();
        alert.setDeviceId(deviceId);
        alert.setRuleId(rule.getId());
        alert.setType(rule.getType());
        alert.setSeverity(rule.getSeverity());
        alert.setStatus(AlertStatus.ACTIVE);
        alert.setMessage(message);
        alert.setObservedValue(observedValue);
        alert.setThreshold(rule.getThreshold());
        alert.setCreatedAt(Instant.now());
        repository.save(alert);
        metrics.alertRaised(rule.getType(), rule.getSeverity());
        broadcaster.broadcastAlert(mapper.toView(alert));
    }

    @Transactional
    public void resolveForRule(String deviceId, Long ruleId) {
        repository.findFirstByDeviceIdAndRuleIdAndStatusIn(deviceId, ruleId, OPEN)
                .ifPresent(this::markResolved);
    }

    @Transactional
    public void raiseOffline(String deviceId, AlertRule rule, String message) {
        if (repository.findFirstByDeviceIdAndTypeAndStatusIn(deviceId, AlertType.OFFLINE, OPEN).isPresent()) {
            return;
        }
        Alert alert = new Alert();
        alert.setDeviceId(deviceId);
        alert.setRuleId(rule.getId());
        alert.setType(AlertType.OFFLINE);
        alert.setSeverity(rule.getSeverity());
        alert.setStatus(AlertStatus.ACTIVE);
        alert.setMessage(message);
        alert.setCreatedAt(Instant.now());
        repository.save(alert);
        metrics.alertRaised(AlertType.OFFLINE, rule.getSeverity());
        broadcaster.broadcastAlert(mapper.toView(alert));
    }

    @Transactional
    public void resolveOffline(String deviceId) {
        repository.findFirstByDeviceIdAndTypeAndStatusIn(deviceId, AlertType.OFFLINE, OPEN)
                .ifPresent(this::markResolved);
    }

    @Transactional
    public AlertView acknowledge(Long id) {
        Alert alert = get(id);
        if (alert.getStatus() == AlertStatus.ACTIVE) {
            alert.setStatus(AlertStatus.ACKNOWLEDGED);
            alert.setAcknowledgedAt(Instant.now());
            repository.save(alert);
            broadcaster.broadcastAlert(mapper.toView(alert));
        }
        return mapper.toView(alert);
    }

    @Transactional
    public AlertView resolve(Long id) {
        Alert alert = get(id);
        if (alert.getStatus() != AlertStatus.RESOLVED) {
            markResolved(alert);
        }
        return mapper.toView(alert);
    }

    @Transactional(readOnly = true)
    public PageResponse<AlertView> search(AlertStatus status, String deviceId, Pageable pageable) {
        return PageResponse.of(repository.search(status, deviceId, pageable).map(mapper::toView));
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus(AlertStatus.ACTIVE);
    }

    private void markResolved(Alert alert) {
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(Instant.now());
        repository.save(alert);
        broadcaster.broadcastAlert(mapper.toView(alert));
    }

    private Alert get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert %d not found".formatted(id)));
    }
}
