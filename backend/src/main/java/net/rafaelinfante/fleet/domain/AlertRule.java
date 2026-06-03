package net.rafaelinfante.fleet.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A configurable alerting rule. THRESHOLD rules compare a {@link Metric} against {@code threshold}
 * with {@code operator}; GEOFENCE rules test the position against {@code geofence} in the given
 * {@link GeofenceMode}; OFFLINE rules are evaluated by the offline-detection job and the LWT handler.
 * An optional {@code deviceType} scopes the rule to one class of vehicle.
 */
@Entity
@Table(name = "alert_rule")
@Getter
@Setter
@NoArgsConstructor
public class AlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Metric metric;

    @Enumerated(EnumType.STRING)
    @Column(length = 8)
    private ComparisonOperator operator;

    private Double threshold;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "geofence_id", foreignKey = @ForeignKey(name = "fk_rule_geofence"))
    private Geofence geofence;

    @Enumerated(EnumType.STRING)
    @Column(name = "geofence_mode", length = 8)
    private GeofenceMode geofenceMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 16)
    private DeviceType deviceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AlertSeverity severity;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
