package net.rafaelinfante.fleet.web.dto;

import java.time.Instant;

public record ReadingView(
        Long id,
        String deviceId,
        Instant recordedAt,
        Instant receivedAt,
        double latitude,
        double longitude,
        double speedKmh,
        Double batteryPct,
        Double fuelPct,
        Double engineTempC,
        Double odometerKm) {
}
