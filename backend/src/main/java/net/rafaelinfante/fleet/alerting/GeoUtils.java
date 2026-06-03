package net.rafaelinfante.fleet.alerting;

public final class GeoUtils {

    private static final double EARTH_RADIUS_M = 6_371_000.0;

    private GeoUtils() {
    }

    /** Great-circle distance in metres between two WGS-84 points. */
    public static double haversineMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        // Clamp guards against floating-point drift pushing 'a' just past 1 for (near-)antipodal points.
        return EARTH_RADIUS_M * 2 * Math.asin(Math.sqrt(Math.min(1.0, a)));
    }
}
