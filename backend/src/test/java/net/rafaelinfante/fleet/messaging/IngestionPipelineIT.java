package net.rafaelinfante.fleet.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import net.rafaelinfante.fleet.domain.AlertSeverity;
import net.rafaelinfante.fleet.domain.AlertStatus;
import net.rafaelinfante.fleet.domain.AlertType;
import net.rafaelinfante.fleet.domain.DeviceType;
import net.rafaelinfante.fleet.domain.TelemetryReadingRepository;
import net.rafaelinfante.fleet.domain.AlertRepository;
import net.rafaelinfante.fleet.mqtt.DeviceTelemetryPayload;
import net.rafaelinfante.fleet.support.AbstractIntegrationTest;

/**
 * Exercises the full path through both brokers: publish over MQTT, watch it travel
 * MQTT -> ingestion -> RabbitMQ -> consumers -> MySQL, raise an alert, and dead-letter a
 * corrupt packet. Readings are republished while polling, which both removes any
 * adapter-startup race and demonstrates that redelivery is idempotent.
 */
class IngestionPipelineIT extends AbstractIntegrationTest {

    @Autowired
    private TelemetryReadingRepository readingRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private ObjectMapper objectMapper;

    private MqttAsyncClient publisher;

    @BeforeEach
    void connectPublisher() throws Exception {
        publisher = new MqttAsyncClient(mqttBrokerUrl(), "it-publisher", new MemoryPersistence());
        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setServerURIs(new String[]{mqttBrokerUrl()});
        publisher.connect(options).waitForCompletion(5_000);
    }

    @AfterEach
    void disconnectPublisher() throws Exception {
        publisher.disconnect().waitForCompletion(2_000);
        publisher.close();
    }

    @Test
    void readingTravelsBothBrokersAndRaisesAnAlert() {
        String deviceId = "VH-IT-SPEED";
        Instant recordedAt = Instant.parse("2026-06-26T10:15:30Z");
        DeviceTelemetryPayload speeding = new DeviceTelemetryPayload(
                deviceId, DeviceType.VAN, recordedAt, 53.349, -6.26, 205.0, 80.0, 70.0, 90.0, 1000.0);

        await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            publish(deviceId, speeding);
            assertThat(readingRepository.existsByDeviceIdAndRecordedAt(deviceId, recordedAt)).isTrue();
            assertThat(alertRepository.search(AlertStatus.ACTIVE, deviceId, PageRequest.of(0, 10)).getContent())
                    .anyMatch(a -> a.getType() == AlertType.THRESHOLD && a.getSeverity() == AlertSeverity.CRITICAL);
        });
    }

    @Test
    void corruptReadingIsDeadLettered() {
        String deviceId = "VH-IT-CORRUPT";
        Instant recordedAt = Instant.parse("2026-06-26T10:20:00Z");
        DeviceTelemetryPayload corrupt = new DeviceTelemetryPayload(
                deviceId, DeviceType.CAR, recordedAt, 999.0, 999.0, 40.0, 80.0, 70.0, 90.0, 1000.0);

        await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            publish(deviceId, corrupt);
            Counter dlq = meterRegistry.find("fleet.dlq.messages")
                    .tag("queue", RabbitConfig.QUEUE_PERSIST).counter();
            assertThat(dlq).isNotNull();
            assertThat(dlq.count()).isGreaterThanOrEqualTo(1.0);
        });
        assertThat(readingRepository.existsByDeviceIdAndRecordedAt(deviceId, recordedAt)).isFalse();
    }

    @Test
    void redeliveredReadingIsStoredOnce() throws Exception {
        String deviceId = "VH-IT-DEDUP";
        Instant recordedAt = Instant.parse("2026-06-26T10:25:00Z");
        DeviceTelemetryPayload reading = new DeviceTelemetryPayload(
                deviceId, DeviceType.TRUCK, recordedAt, 53.34, -6.25, 50.0, 80.0, 70.0, 90.0, 1000.0);

        await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            publish(deviceId, reading);
            assertThat(readingRepository.existsByDeviceIdAndRecordedAt(deviceId, recordedAt)).isTrue();
        });
        // A few more identical deliveries must not create new rows.
        publish(deviceId, reading);
        publish(deviceId, reading);
        await().during(Duration.ofSeconds(3)).atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(readingRepository.findByDeviceId(deviceId, PageRequest.of(0, 10)).getTotalElements())
                        .isEqualTo(1L));
    }

    private void publish(String deviceId, DeviceTelemetryPayload payload) throws Exception {
        MqttMessage message = new MqttMessage(objectMapper.writeValueAsBytes(payload));
        message.setQos(1);
        publisher.publish("fleet/" + deviceId + "/telemetry", message).waitForCompletion(2_000);
    }
}
