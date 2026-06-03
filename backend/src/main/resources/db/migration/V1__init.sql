CREATE TABLE device (
    device_id          VARCHAR(64)  NOT NULL,
    name               VARCHAR(128) NOT NULL,
    type               VARCHAR(16)  NOT NULL,
    status             VARCHAR(16)  NOT NULL,
    first_seen_at      DATETIME(6)  NOT NULL,
    last_seen_at       DATETIME(6)  NULL,
    last_latitude      DOUBLE       NULL,
    last_longitude     DOUBLE       NULL,
    last_speed_kmh     DOUBLE       NULL,
    last_battery_pct   DOUBLE       NULL,
    last_fuel_pct      DOUBLE       NULL,
    last_engine_temp_c DOUBLE       NULL,
    last_odometer_km   DOUBLE       NULL,
    version            BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (device_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE telemetry_reading (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    device_id     VARCHAR(64) NOT NULL,
    recorded_at   DATETIME(6) NOT NULL,
    received_at   DATETIME(6) NOT NULL,
    latitude      DOUBLE      NOT NULL,
    longitude     DOUBLE      NOT NULL,
    speed_kmh     DOUBLE      NOT NULL,
    battery_pct   DOUBLE      NULL,
    fuel_pct      DOUBLE      NULL,
    engine_temp_c DOUBLE      NULL,
    odometer_km   DOUBLE      NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_reading_device_time UNIQUE (device_id, recorded_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE geofence (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(128) NOT NULL,
    center_lat DOUBLE       NOT NULL,
    center_lng DOUBLE       NOT NULL,
    radius_m   DOUBLE       NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE alert_rule (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(128) NOT NULL,
    type          VARCHAR(16)  NOT NULL,
    metric        VARCHAR(16)  NULL,
    operator      VARCHAR(8)   NULL,
    threshold     DOUBLE       NULL,
    geofence_id   BIGINT       NULL,
    geofence_mode VARCHAR(8)   NULL,
    device_type   VARCHAR(16)  NULL,
    severity      VARCHAR(16)  NOT NULL,
    enabled       BIT(1)       NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_rule_geofence FOREIGN KEY (geofence_id) REFERENCES geofence (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE alert (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    device_id       VARCHAR(64)  NOT NULL,
    rule_id         BIGINT       NULL,
    type            VARCHAR(16)  NOT NULL,
    severity        VARCHAR(16)  NOT NULL,
    status          VARCHAR(16)  NOT NULL,
    message         VARCHAR(512) NOT NULL,
    observed_value  DOUBLE       NULL,
    threshold       DOUBLE       NULL,
    created_at      DATETIME(6)  NOT NULL,
    acknowledged_at DATETIME(6)  NULL,
    resolved_at     DATETIME(6)  NULL,
    PRIMARY KEY (id),
    KEY idx_alert_device (device_id),
    KEY idx_alert_status (status),
    KEY idx_alert_created (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
