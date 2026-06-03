package net.rafaelinfante.fleet.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A tracked vehicle. The {@code deviceId} is the natural key reported by the device itself;
 * a row is created on first contact (auto-registration) and then carries a rolling snapshot
 * of the latest reading so the dashboard can render current state without scanning the
 * time-series table.
 */
@Entity
@Table(name = "device")
@Getter
@Setter
@NoArgsConstructor
public class Device {

    @Id
    @Column(name = "device_id", length = 64, nullable = false)
    private String deviceId;

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DeviceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DeviceStatus status = DeviceStatus.UNKNOWN;

    @Column(name = "first_seen_at", nullable = false)
    private Instant firstSeenAt;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "last_latitude")
    private Double lastLatitude;

    @Column(name = "last_longitude")
    private Double lastLongitude;

    @Column(name = "last_speed_kmh")
    private Double lastSpeedKmh;

    @Column(name = "last_battery_pct")
    private Double lastBatteryPct;

    @Column(name = "last_fuel_pct")
    private Double lastFuelPct;

    @Column(name = "last_engine_temp_c")
    private Double lastEngineTempC;

    @Column(name = "last_odometer_km")
    private Double lastOdometerKm;

    // Optimistic lock: guards the live snapshot against the offline sweep clobbering a fresh reading.
    @Version
    @Column(nullable = false)
    private Long version;

    public Device(String deviceId, String name, DeviceType type, Instant firstSeenAt) {
        this.deviceId = deviceId;
        this.name = name;
        this.type = type;
        this.firstSeenAt = firstSeenAt;
    }
}
