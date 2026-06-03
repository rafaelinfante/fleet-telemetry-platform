package net.rafaelinfante.fleet.messaging;

import java.time.Instant;

import net.rafaelinfante.fleet.domain.DeviceStatus;
import net.rafaelinfante.fleet.domain.DeviceType;

/**
 * The normalised event the MQTT bridge publishes onto RabbitMQ. A READING carries the
 * measurement fields; a STATUS carries an online/offline transition (from the device's
 * connect message or its last-will).
 */
public record TelemetryEvent(
        EventType eventType,
        String deviceId,
        DeviceType deviceType,
        Instant recordedAt,
        Double latitude,
        Double longitude,
        Double speedKmh,
        Double batteryPct,
        Double fuelPct,
        Double engineTempC,
        Double odometerKm,
        DeviceStatus status) {

    public static TelemetryEvent reading(String deviceId, DeviceType deviceType, Instant recordedAt,
                                         Double latitude, Double longitude, Double speedKmh,
                                         Double batteryPct, Double fuelPct, Double engineTempC, Double odometerKm) {
        return new TelemetryEvent(EventType.READING, deviceId, deviceType, recordedAt,
                latitude, longitude, speedKmh, batteryPct, fuelPct, engineTempC, odometerKm, null);
    }

    public static TelemetryEvent status(String deviceId, Instant recordedAt, DeviceStatus status) {
        return new TelemetryEvent(EventType.STATUS, deviceId, null, recordedAt,
                null, null, null, null, null, null, null, status);
    }
}
