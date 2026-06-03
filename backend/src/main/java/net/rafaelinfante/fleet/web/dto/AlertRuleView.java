package net.rafaelinfante.fleet.web.dto;

import java.time.Instant;

import net.rafaelinfante.fleet.domain.AlertSeverity;
import net.rafaelinfante.fleet.domain.AlertType;
import net.rafaelinfante.fleet.domain.ComparisonOperator;
import net.rafaelinfante.fleet.domain.DeviceType;
import net.rafaelinfante.fleet.domain.GeofenceMode;
import net.rafaelinfante.fleet.domain.Metric;

public record AlertRuleView(
        Long id,
        String name,
        AlertType type,
        Metric metric,
        ComparisonOperator operator,
        Double threshold,
        Long geofenceId,
        String geofenceName,
        GeofenceMode geofenceMode,
        DeviceType deviceType,
        AlertSeverity severity,
        boolean enabled,
        Instant createdAt) {
}
