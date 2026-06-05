package net.rafaelinfante.fleet.simulator;

/** Status payload published (retained) to {@code fleet/{deviceId}/status}; also used as the last-will. */
public record StatusMessage(
        String deviceId,
        String status) {
}
