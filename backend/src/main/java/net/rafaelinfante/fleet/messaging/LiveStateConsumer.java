package net.rafaelinfante.fleet.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import net.rafaelinfante.fleet.domain.Device;
import net.rafaelinfante.fleet.service.DeviceService;
import net.rafaelinfante.fleet.web.TelemetryBroadcaster;
import net.rafaelinfante.fleet.web.mapper.DeviceMapper;

/**
 * Keeps each device's live snapshot current and pushes it to the dashboard. Single consumer so
 * snapshot updates for a device stay ordered (the freshest reading wins).
 */
@Component
public class LiveStateConsumer {

    private final DeviceService deviceService;
    private final TelemetryBroadcaster broadcaster;
    private final DeviceMapper deviceMapper;

    public LiveStateConsumer(DeviceService deviceService, TelemetryBroadcaster broadcaster, DeviceMapper deviceMapper) {
        this.deviceService = deviceService;
        this.broadcaster = broadcaster;
        this.deviceMapper = deviceMapper;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_LIVE, concurrency = "1")
    public void onEvent(TelemetryEvent event) {
        Device device;
        if (event.eventType() == EventType.READING) {
            if (!ReadingValidator.isValidReading(event)) {
                return;
            }
            device = deviceService.applyReading(event);
        } else {
            device = deviceService.applyStatus(event.deviceId(), event.status(), event.recordedAt());
        }
        broadcaster.broadcastDevice(deviceMapper.toView(device));
    }
}
