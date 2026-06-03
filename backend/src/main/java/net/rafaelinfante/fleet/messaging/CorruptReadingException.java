package net.rafaelinfante.fleet.messaging;

/** Thrown when a reading cannot be trusted (missing or out-of-range fields). Drives the message to the DLQ. */
public class CorruptReadingException extends RuntimeException {

    public CorruptReadingException(String message) {
        super(message);
    }
}
