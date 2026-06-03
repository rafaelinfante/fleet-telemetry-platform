package net.rafaelinfante.fleet.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single time-series sample. {@code deviceId} is stored denormalised (no FK) so ingestion
 * never blocks on device-row creation ordering; the unique (deviceId, recordedAt) constraint
 * is what makes redelivered messages idempotent.
 */
@Entity
@Table(
        name = "telemetry_reading",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reading_device_time", columnNames = {"device_id", "recorded_at"}))
@Getter
@Setter
@NoArgsConstructor
public class TelemetryReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", length = 64, nullable = false)
    private String deviceId;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(name = "speed_kmh", nullable = false)
    private double speedKmh;

    @Column(name = "battery_pct")
    private Double batteryPct;

    @Column(name = "fuel_pct")
    private Double fuelPct;

    @Column(name = "engine_temp_c")
    private Double engineTempC;

    @Column(name = "odometer_km")
    private Double odometerKm;
}
