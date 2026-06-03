package net.rafaelinfante.fleet.observability;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import net.rafaelinfante.fleet.domain.AlertSeverity;
import net.rafaelinfante.fleet.domain.AlertType;

/**
 * Central place for the application's custom counters so metric names and tags stay consistent.
 * Micrometer caches meters by id, so resolving them per call is cheap.
 */
@Component
public class FleetMetrics {

    private final MeterRegistry registry;

    public FleetMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void ingested(String deviceType) {
        registry.counter("fleet.mqtt.ingested", "device_type", deviceType).increment();
    }

    public void mqttError() {
        registry.counter("fleet.mqtt.errors").increment();
    }

    public void published() {
        registry.counter("fleet.rabbit.published").increment();
    }

    public void persisted() {
        registry.counter("fleet.readings.persisted").increment();
    }

    public void duplicate() {
        registry.counter("fleet.readings.duplicates").increment();
    }

    public void alertRaised(AlertType type, AlertSeverity severity) {
        registry.counter("fleet.alerts.raised", "type", type.name(), "severity", severity.name()).increment();
    }

    public void deadLettered(String queue) {
        registry.counter("fleet.dlq.messages", "queue", queue).increment();
    }
}
