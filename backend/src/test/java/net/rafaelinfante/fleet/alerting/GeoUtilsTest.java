package net.rafaelinfante.fleet.alerting;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GeoUtilsTest {

    @Test
    void samePointIsZeroDistance() {
        assertThat(GeoUtils.haversineMeters(53.3498, -6.2603, 53.3498, -6.2603)).isZero();
    }

    @Test
    void oneDegreeOfLongitudeAtEquatorIsAboutOneEleventhOfEarth() {
        double meters = GeoUtils.haversineMeters(0, 0, 0, 1);
        assertThat(meters).isCloseTo(111_195, org.assertj.core.data.Offset.offset(200.0));
    }

    @Test
    void knownCityDistanceIsReasonable() {
        // Dublin city centre to Dublin Airport is roughly 9-10 km.
        double meters = GeoUtils.haversineMeters(53.3498, -6.2603, 53.4264, -6.2499);
        assertThat(meters).isBetween(8_000.0, 10_000.0);
    }

    @Test
    void antipodalPointsDoNotReturnNaN() {
        double meters = GeoUtils.haversineMeters(
                -84.3447170112, -51.1488289882, 84.3447170112, 128.8511710118);
        assertThat(meters).isNotNaN()
                .isCloseTo(20_015_000, org.assertj.core.data.Offset.offset(100_000.0));
    }
}
