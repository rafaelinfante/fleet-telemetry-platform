package net.rafaelinfante.fleet.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import net.rafaelinfante.fleet.domain.DeviceType;

class ReadingValidatorTest {

    private static TelemetryEvent reading(double lat, double lng, Double speed) {
        return TelemetryEvent.reading("VH-1", DeviceType.VAN, Instant.now(), lat, lng, speed, 80.0, 70.0, 90.0, 1.0);
    }

    @Test
    void acceptsPlausibleReading() {
        assertThat(ReadingValidator.isValidReading(reading(53.3, -6.2, 60.0))).isTrue();
        assertThatCode(() -> ReadingValidator.validate(reading(53.3, -6.2, 60.0))).doesNotThrowAnyException();
    }

    @Test
    void rejectsOutOfRangeCoordinates() {
        assertThat(ReadingValidator.isValidReading(reading(999, 999, 60.0))).isFalse();
        assertThatThrownBy(() -> ReadingValidator.validate(reading(999, 999, 60.0)))
                .isInstanceOf(CorruptReadingException.class);
    }

    @Test
    void rejectsImplausibleSpeedAndMissingFields() {
        assertThat(ReadingValidator.isValidReading(reading(53.3, -6.2, 950.0))).isFalse();
        assertThat(ReadingValidator.isValidReading(reading(53.3, -6.2, null))).isFalse();
    }
}
