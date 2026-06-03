-- Geofences over the simulated operating region (Dublin).
INSERT INTO geofence (name, center_lat, center_lng, radius_m, created_at) VALUES
    ('Greater Dublin operating area', 53.349800, -6.260300, 12000, UTC_TIMESTAMP(6)),
    ('Dublin Airport restricted zone', 53.426400, -6.249900, 2500, UTC_TIMESTAMP(6));

-- Default alerting rules.
INSERT INTO alert_rule (name, type, metric, operator, threshold, geofence_id, geofence_mode, device_type, severity, enabled, created_at) VALUES
    ('Overspeed (> 120 km/h)',        'THRESHOLD', 'SPEED',       'GT',  120, NULL, NULL,    NULL, 'WARNING',  b'1', UTC_TIMESTAMP(6)),
    ('Severe overspeed (> 150 km/h)', 'THRESHOLD', 'SPEED',       'GT',  150, NULL, NULL,    NULL, 'CRITICAL', b'1', UTC_TIMESTAMP(6)),
    ('Low battery (< 15%)',           'THRESHOLD', 'BATTERY',     'LT',  15,  NULL, NULL,    NULL, 'WARNING',  b'1', UTC_TIMESTAMP(6)),
    ('Low fuel (< 10%)',              'THRESHOLD', 'FUEL',        'LT',  10,  NULL, NULL,    NULL, 'WARNING',  b'1', UTC_TIMESTAMP(6)),
    ('Engine overheating (> 110 C)',  'THRESHOLD', 'ENGINE_TEMP', 'GT',  110, NULL, NULL,    NULL, 'CRITICAL', b'1', UTC_TIMESTAMP(6)),
    ('Left operating area',           'GEOFENCE',  NULL,          NULL,  NULL, 1,   'EXIT',  NULL, 'WARNING',  b'1', UTC_TIMESTAMP(6)),
    ('Entered restricted airport zone','GEOFENCE', NULL,          NULL,  NULL, 2,   'ENTER', NULL, 'CRITICAL', b'1', UTC_TIMESTAMP(6)),
    ('Device offline',                'OFFLINE',   NULL,          NULL,  NULL, NULL, NULL,   NULL, 'WARNING',  b'1', UTC_TIMESTAMP(6));
