package net.rafaelinfante.fleet.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.rafaelinfante.fleet.domain.AlertRule;
import net.rafaelinfante.fleet.domain.AlertRuleRepository;
import net.rafaelinfante.fleet.domain.AlertType;
import net.rafaelinfante.fleet.domain.Geofence;
import net.rafaelinfante.fleet.domain.GeofenceRepository;
import net.rafaelinfante.fleet.web.dto.AlertRuleView;
import net.rafaelinfante.fleet.web.dto.RuleRequest;
import net.rafaelinfante.fleet.web.error.ResourceNotFoundException;
import net.rafaelinfante.fleet.web.mapper.AlertRuleMapper;

@Service
public class RuleService {

    private final AlertRuleRepository ruleRepository;
    private final GeofenceRepository geofenceRepository;
    private final AlertRuleMapper mapper;

    public RuleService(AlertRuleRepository ruleRepository, GeofenceRepository geofenceRepository,
                       AlertRuleMapper mapper) {
        this.ruleRepository = ruleRepository;
        this.geofenceRepository = geofenceRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<AlertRuleView> findAll() {
        return ruleRepository.findAll().stream().map(mapper::toView).toList();
    }

    @Transactional(readOnly = true)
    public AlertRuleView findById(Long id) {
        return mapper.toView(get(id));
    }

    @Transactional
    public AlertRuleView create(RuleRequest request) {
        AlertRule rule = apply(new AlertRule(), request);
        rule.setCreatedAt(Instant.now());
        return mapper.toView(ruleRepository.save(rule));
    }

    @Transactional
    public AlertRuleView update(Long id, RuleRequest request) {
        return mapper.toView(ruleRepository.save(apply(get(id), request)));
    }

    @Transactional
    public void delete(Long id) {
        if (!ruleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Rule %d not found".formatted(id));
        }
        ruleRepository.deleteById(id);
    }

    private AlertRule apply(AlertRule rule, RuleRequest request) {
        validate(request);
        rule.setName(request.name());
        rule.setType(request.type());
        rule.setMetric(request.metric());
        rule.setOperator(request.operator());
        rule.setThreshold(request.threshold());
        rule.setGeofence(resolveGeofence(request.geofenceId()));
        rule.setGeofenceMode(request.geofenceMode());
        rule.setDeviceType(request.deviceType());
        rule.setSeverity(request.severity());
        rule.setEnabled(request.enabled() == null || request.enabled());
        return rule;
    }

    private static void validate(RuleRequest request) {
        if (request.type() == AlertType.THRESHOLD
                && (request.metric() == null || request.operator() == null || request.threshold() == null)) {
            throw new IllegalArgumentException("Threshold rules require metric, operator and threshold");
        }
        if (request.type() == AlertType.GEOFENCE
                && (request.geofenceId() == null || request.geofenceMode() == null)) {
            throw new IllegalArgumentException("Geofence rules require geofenceId and geofenceMode");
        }
    }

    private Geofence resolveGeofence(Long geofenceId) {
        if (geofenceId == null) {
            return null;
        }
        return geofenceRepository.findById(geofenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Geofence %d not found".formatted(geofenceId)));
    }

    private AlertRule get(Long id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule %d not found".formatted(id)));
    }
}
