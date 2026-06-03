package net.rafaelinfante.fleet.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.rafaelinfante.fleet.domain.TelemetryReading;
import net.rafaelinfante.fleet.domain.TelemetryReadingRepository;
import net.rafaelinfante.fleet.observability.FleetMetrics;

/**
 * The system of record. Validates each reading strictly (corrupt ones are rejected to the DLQ)
 * and stores it idempotently — a redelivered or duplicate message is recognised by the unique
 * (deviceId, recordedAt) constraint and counted rather than stored twice. Runs with the default
 * concurrency so persistence scales out under load.
 */
@Component
public class TelemetryPersistConsumer {

    private final TelemetryReadingRepository repository;
    private final FleetMetrics metrics;

    public TelemetryPersistConsumer(TelemetryReadingRepository repository, FleetMetrics metrics) {
        this.repository = repository;
        this.metrics = metrics;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_PERSIST)
    @Transactional
    public void onReading(TelemetryEvent event) {
        ReadingValidator.validate(event);
        if (repository.existsByDeviceIdAndRecordedAt(event.deviceId(), event.recordedAt())) {
            metrics.duplicate();
            return;
        }
        repository.save(toEntity(event));
        metrics.persisted();
    }

    private static TelemetryReading toEntity(TelemetryEvent event) {
        TelemetryReading reading = new TelemetryReading();
        reading.setDeviceId(event.deviceId());
        reading.setRecordedAt(event.recordedAt());
        reading.setReceivedAt(java.time.Instant.now());
        reading.setLatitude(event.latitude());
        reading.setLongitude(event.longitude());
        reading.setSpeedKmh(event.speedKmh());
        reading.setBatteryPct(event.batteryPct());
        reading.setFuelPct(event.fuelPct());
        reading.setEngineTempC(event.engineTempC());
        reading.setOdometerKm(event.odometerKm());
        return reading;
    }
}
