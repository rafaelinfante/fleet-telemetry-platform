package net.rafaelinfante.fleet.mqtt;

/** JSON shape a device publishes (retained) to {@code fleet/{deviceId}/status}, also used as its last-will. */
public record DeviceStatusPayload(
        String deviceId,
        String status) {
}
