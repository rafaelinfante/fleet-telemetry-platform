package net.rafaelinfante.fleet.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A raised alert. At most one ACTIVE alert exists per (device, rule) — or per (device, OFFLINE) —
 * at a time; it auto-resolves when the condition clears.
 */
@Entity
@Table(
        name = "alert",
        indexes = {
                @Index(name = "idx_alert_device", columnList = "device_id"),
                @Index(name = "idx_alert_status", columnList = "status"),
                @Index(name = "idx_alert_created", columnList = "created_at")
        })
@Getter
@Setter
@NoArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", length = 64, nullable = false)
    private String deviceId;

    @Column(name = "rule_id")
    private Long ruleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AlertStatus status = AlertStatus.ACTIVE;

    @Column(nullable = false, length = 512)
    private String message;

    @Column(name = "observed_value")
    private Double observedValue;

    private Double threshold;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;
}
