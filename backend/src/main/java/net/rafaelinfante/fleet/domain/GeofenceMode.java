package net.rafaelinfante.fleet.domain;

public enum GeofenceMode {
    /** Raise an alert while the device is inside the fenced area. */
    ENTER,
    /** Raise an alert while the device is outside the fenced area. */
    EXIT
}
