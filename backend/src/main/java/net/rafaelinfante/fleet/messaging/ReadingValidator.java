package net.rafaelinfante.fleet.messaging;

/** Plausibility checks shared by every reading consumer. */
public final class ReadingValidator {

    private static final double MAX_PLAUSIBLE_SPEED_KMH = 400.0;

    private ReadingValidator() {
    }

    public static boolean isValidReading(TelemetryEvent event) {
        if (event.deviceId() == null || event.recordedAt() == null) {
            return false;
        }
        if (event.latitude() == null || event.longitude() == null || event.speedKmh() == null) {
            return false;
        }
        double lat = event.latitude();
        double lng = event.longitude();
        double speed = event.speedKmh();
        return lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180
                && speed >= 0 && speed <= MAX_PLAUSIBLE_SPEED_KMH;
    }

    /** Throws {@link CorruptReadingException} for the system of record (drives the message to the DLQ). */
    public static void validate(TelemetryEvent event) {
        if (!isValidReading(event)) {
            throw new CorruptReadingException("corrupt reading for device " + event.deviceId());
        }
    }
}
