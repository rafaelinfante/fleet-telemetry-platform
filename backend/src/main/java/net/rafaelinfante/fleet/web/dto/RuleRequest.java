package net.rafaelinfante.fleet.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.rafaelinfante.fleet.domain.AlertSeverity;
import net.rafaelinfante.fleet.domain.AlertType;
import net.rafaelinfante.fleet.domain.ComparisonOperator;
import net.rafaelinfante.fleet.domain.DeviceType;
import net.rafaelinfante.fleet.domain.GeofenceMode;
import net.rafaelinfante.fleet.domain.Metric;

public record RuleRequest(
        @NotBlank String name,
        @NotNull AlertType type,
        Metric metric,
        ComparisonOperator operator,
        Double threshold,
        Long geofenceId,
        GeofenceMode geofenceMode,
        DeviceType deviceType,
        @NotNull AlertSeverity severity,
        Boolean enabled) {
}
