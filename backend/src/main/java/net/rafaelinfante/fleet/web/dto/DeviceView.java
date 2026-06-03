package net.rafaelinfante.fleet.web.dto;

import java.time.Instant;

import net.rafaelinfante.fleet.domain.DeviceStatus;
import net.rafaelinfante.fleet.domain.DeviceType;

public record DeviceView(
        String deviceId,
        String name,
        DeviceType type,
        DeviceStatus status,
        Instant firstSeenAt,
        Instant lastSeenAt,
        Double lastLatitude,
        Double lastLongitude,
        Double lastSpeedKmh,
        Double lastBatteryPct,
        Double lastFuelPct,
        Double lastEngineTempC,
        Double lastOdometerKm) {
}
