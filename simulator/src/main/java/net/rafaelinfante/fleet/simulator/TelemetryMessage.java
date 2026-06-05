package net.rafaelinfante.fleet.simulator;

import java.time.Instant;

/** Telemetry payload published to {@code fleet/{deviceId}/telemetry}. */
public record TelemetryMessage(
        String deviceId,
        String type,
        Instant timestamp,
        double lat,
        double lng,
        double speedKmh,
        double batteryPct,
        double fuelPct,
        double engineTempC,
        double odometerKm) {
}
