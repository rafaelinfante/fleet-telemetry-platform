package net.rafaelinfante.fleet.simulator;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Random;

import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttMessage;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Emulates one vehicle: holds its own MQTT v5 connection (with a retained last-will so an
 * ungraceful drop is seen as OFFLINE), walks a plausible track around the region, drains
 * battery/fuel and occasionally emits an anomaly or a corrupt packet.
 */
@Slf4j
class SimulatedDevice {

    private static final double KM_PER_DEG_LAT = 111.0;

    private final String deviceId;
    private final String type;
    private final String brokerUrl;
    private final ObjectMapper objectMapper;
    private final Random random;

    private double latitude;
    private double longitude;
    private double headingDeg;
    private double speedKmh;
    private double batteryPct;
    private double fuelPct;
    private double engineTempC;
    private double odometerKm;

    private MqttAsyncClient client;
    private int offlineTicks;

    SimulatedDevice(String deviceId, String type, double centerLat, double centerLng, double spreadKm,
                    String brokerUrl, ObjectMapper objectMapper, long seed) {
        this.deviceId = deviceId;
        this.type = type;
        this.brokerUrl = brokerUrl;
        this.objectMapper = objectMapper;
        this.random = new Random(seed);

        double lngScale = KM_PER_DEG_LAT * Math.cos(Math.toRadians(centerLat));
        this.latitude = centerLat + (random.nextDouble() * 2 - 1) * (spreadKm / KM_PER_DEG_LAT);
        this.longitude = centerLng + (random.nextDouble() * 2 - 1) * (spreadKm / lngScale);
        this.headingDeg = random.nextDouble() * 360;
        this.speedKmh = 30 + random.nextDouble() * 30;
        this.batteryPct = 60 + random.nextDouble() * 40;
        this.fuelPct = 40 + random.nextDouble() * 60;
        this.engineTempC = 85 + random.nextDouble() * 8;
        this.odometerKm = random.nextDouble() * 50_000;
    }

    void connect() throws Exception {
        client = new MqttAsyncClient(brokerUrl, "fleet-sim-" + deviceId, new MemoryPersistence());
        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setServerURIs(new String[]{brokerUrl});
        options.setAutomaticReconnect(false);
        options.setCleanStart(true);
        options.setKeepAliveInterval(30);

        MqttMessage will = new MqttMessage(json(new StatusMessage(deviceId, "OFFLINE")));
        will.setQos(1);
        will.setRetained(true);
        options.setWill(statusTopic(), will);

        client.connect(options).waitForCompletion(5_000);
        publishStatus("ONLINE");
    }

    void publishTick(double intervalSeconds, double anomalyRate, double corruptRate) {
        advance(intervalSeconds);
        boolean corrupt = random.nextDouble() < corruptRate;
        boolean anomaly = !corrupt && random.nextDouble() < anomalyRate;
        if (anomaly) {
            applyAnomaly();
        }
        TelemetryMessage message = corrupt ? corruptReading() : reading();
        publish(telemetryTopic(), message, false);
    }

    boolean isOffline() {
        return offlineTicks > 0;
    }

    /** Drop the connection without a DISCONNECT so the broker publishes the last-will. */
    void goOffline(int ticks) {
        offlineTicks = ticks;
        try {
            if (client != null && client.isConnected()) {
                client.disconnectForcibly(0, 500, false);
            }
        } catch (Exception ex) {
            log.debug("Forced disconnect of {} failed: {}", deviceId, ex.getMessage());
        }
        log.info("Device {} went offline for {} ticks", deviceId, ticks);
    }

    /** Returns true once the device has come back online. */
    boolean tickOffline() {
        if (--offlineTicks > 0) {
            return false;
        }
        try {
            closeQuietly();
            connect();
            log.info("Device {} back online", deviceId);
        } catch (Exception ex) {
            log.warn("Device {} failed to reconnect: {}", deviceId, ex.getMessage());
            offlineTicks = 1;
        }
        return true;
    }

    void shutdown() {
        try {
            if (client != null && client.isConnected()) {
                publishStatus("OFFLINE");
                client.disconnect().waitForCompletion(1_000);
            }
        } catch (Exception ex) {
            log.debug("Shutdown of {} failed: {}", deviceId, ex.getMessage());
        } finally {
            closeQuietly();
        }
    }

    private void advance(double seconds) {
        if (random.nextDouble() < 0.2) {
            headingDeg = (headingDeg + (random.nextDouble() * 90 - 45) + 360) % 360;
        }
        speedKmh = clamp(speedKmh + (random.nextDouble() * 10 - 5), 0, 120);
        double distanceKm = speedKmh * (seconds / 3600.0);
        double lngScale = KM_PER_DEG_LAT * Math.cos(Math.toRadians(latitude));
        latitude += distanceKm / KM_PER_DEG_LAT * Math.cos(Math.toRadians(headingDeg));
        longitude += distanceKm / lngScale * Math.sin(Math.toRadians(headingDeg));
        odometerKm += distanceKm;
        batteryPct = clamp(batteryPct - random.nextDouble() * 0.3, 0, 100);
        fuelPct = clamp(fuelPct - random.nextDouble() * 0.2, 0, 100);
        engineTempC = clamp(85 + random.nextGaussian() * 4, 60, 105);
    }

    private void applyAnomaly() {
        switch (random.nextInt(4)) {
            case 0 -> speedKmh = 130 + random.nextDouble() * 50;
            case 1 -> engineTempC = 112 + random.nextDouble() * 20;
            case 2 -> batteryPct = random.nextDouble() * 14;
            default -> latitude += 0.15; // ~16 km north, out of the operating area
        }
    }

    private TelemetryMessage reading() {
        return new TelemetryMessage(deviceId, type, Instant.now(),
                round(latitude, 6), round(longitude, 6), round(speedKmh, 1),
                round(batteryPct, 1), round(fuelPct, 1), round(engineTempC, 1), round(odometerKm, 1));
    }

    private TelemetryMessage corruptReading() {
        // Valid JSON, impossible coordinates — the persist consumer rejects it to the DLQ.
        return new TelemetryMessage(deviceId, type, Instant.now(),
                999.0, 999.0, round(speedKmh, 1),
                round(batteryPct, 1), round(fuelPct, 1), round(engineTempC, 1), round(odometerKm, 1));
    }

    private void publishStatus(String status) {
        publish(statusTopic(), new StatusMessage(deviceId, status), true);
    }

    private void publish(String topic, Object payload, boolean retained) {
        try {
            MqttMessage message = new MqttMessage(json(payload));
            message.setQos(1);
            message.setRetained(retained);
            client.publish(topic, message);
        } catch (Exception ex) {
            log.warn("Publish to {} failed: {}", topic, ex.getMessage());
        }
    }

    private byte[] json(Object payload) {
        try {
            return objectMapper.writeValueAsBytes(payload);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot serialise payload", ex);
        }
    }

    private void closeQuietly() {
        try {
            if (client != null) {
                client.close(true);
            }
        } catch (Exception ignored) {
            // best effort
        }
    }

    private String telemetryTopic() {
        return "fleet/" + deviceId + "/telemetry";
    }

    private String statusTopic() {
        return "fleet/" + deviceId + "/status";
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double round(double value, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(value * scale) / scale;
    }
}
