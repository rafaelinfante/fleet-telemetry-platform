package net.rafaelinfante.fleet.web.dto;

import java.time.Instant;

import net.rafaelinfante.fleet.domain.AlertSeverity;
import net.rafaelinfante.fleet.domain.AlertStatus;
import net.rafaelinfante.fleet.domain.AlertType;

public record AlertView(
        Long id,
        String deviceId,
        Long ruleId,
        AlertType type,
        AlertSeverity severity,
        AlertStatus status,
        String message,
        Double observedValue,
        Double threshold,
        Instant createdAt,
        Instant acknowledgedAt,
        Instant resolvedAt) {
}
