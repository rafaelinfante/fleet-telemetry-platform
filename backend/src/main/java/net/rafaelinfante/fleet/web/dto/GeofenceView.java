package net.rafaelinfante.fleet.web.dto;

import java.time.Instant;

public record GeofenceView(
        Long id,
        String name,
        double centerLat,
        double centerLng,
        double radiusM,
        Instant createdAt) {
}
