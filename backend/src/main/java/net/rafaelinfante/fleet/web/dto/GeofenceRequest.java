package net.rafaelinfante.fleet.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record GeofenceRequest(
        @NotBlank String name,
        @DecimalMin("-90") @DecimalMax("90") double centerLat,
        @DecimalMin("-180") @DecimalMax("180") double centerLng,
        @Positive double radiusM) {
}
