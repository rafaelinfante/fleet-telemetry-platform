package net.rafaelinfante.fleet.mqtt;

import java.time.Instant;

import net.rafaelinfante.fleet.domain.DeviceType;

/** JSON shape a device publishes to {@code fleet/{deviceId}/telemetry}. */
public record DeviceTelemetryPayload(
        String deviceId,
        DeviceType type,
        Instant timestamp,
        Double lat,
        Double lng,
        Double speedKmh,
        Double batteryPct,
        Double fuelPct,
        Double engineTempC,
        Double odometerKm) {
}
