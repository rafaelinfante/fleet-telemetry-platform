package net.rafaelinfante.fleet.mqtt;

import java.time.Instant;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.rafaelinfante.fleet.domain.DeviceStatus;
import net.rafaelinfante.fleet.messaging.RabbitConfig;
import net.rafaelinfante.fleet.messaging.TelemetryEvent;
import net.rafaelinfante.fleet.observability.FleetMetrics;

/**
 * Bridges the device edge (MQTT) to the processing backbone (RabbitMQ). The device id is taken
 * from the topic, which is always intact, so a corrupt payload still routes to the right device;
 * payloads that cannot be parsed at all are metered and dropped here rather than poisoning Rabbit.
 */
@Component
@Slf4j
public class MqttIngestBridge {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final FleetMetrics metrics;

    public MqttIngestBridge(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper, FleetMetrics metrics) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.metrics = metrics;
    }

    public void handle(byte[] payload, MessageHeaders headers) {
        String topic = (String) headers.get(MqttHeaders.RECEIVED_TOPIC);
        if (topic == null) {
            return;
        }
        String deviceId = deviceIdFromTopic(topic);
        try {
            if (topic.endsWith("/telemetry")) {
                handleTelemetry(deviceId, payload);
            } else if (topic.endsWith("/status")) {
                handleStatus(deviceId, payload);
            }
        } catch (Exception ex) {
            metrics.mqttError();
            log.warn("Dropping unparseable MQTT message on '{}': {}", topic, ex.getMessage());
        }
    }

    private void handleTelemetry(String deviceId, byte[] payload) throws Exception {
        DeviceTelemetryPayload p = objectMapper.readValue(payload, DeviceTelemetryPayload.class);
        TelemetryEvent event = TelemetryEvent.reading(deviceId, p.type(), p.timestamp(),
                p.lat(), p.lng(), p.speedKmh(), p.batteryPct(), p.fuelPct(), p.engineTempC(), p.odometerKm());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.RK_READING_PREFIX + deviceId, event);
        metrics.ingested(p.type() != null ? p.type().name() : "UNKNOWN");
        metrics.published();
    }

    private void handleStatus(String deviceId, byte[] payload) throws Exception {
        DeviceStatusPayload p = objectMapper.readValue(payload, DeviceStatusPayload.class);
        DeviceStatus status = DeviceStatus.valueOf(p.status().toUpperCase());
        TelemetryEvent event = TelemetryEvent.status(deviceId, Instant.now(), status);
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.RK_STATUS_PREFIX + deviceId, event);
        metrics.published();
    }

    private static String deviceIdFromTopic(String topic) {
        String[] parts = topic.split("/");
        return parts.length >= 2 ? parts[1] : "unknown";
    }
}
