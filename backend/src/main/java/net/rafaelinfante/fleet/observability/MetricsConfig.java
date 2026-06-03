package net.rafaelinfante.fleet.observability;

import java.util.List;

import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import net.rafaelinfante.fleet.alerting.AlertService;
import net.rafaelinfante.fleet.messaging.RabbitConfig;
import net.rafaelinfante.fleet.service.DeviceService;

/**
 * Registers live gauges: how many devices are online, how many alerts are active, and the depth of
 * each work queue (read from the broker on scrape). Counters live in {@link FleetMetrics}.
 */
@Configuration
public class MetricsConfig {

    public MetricsConfig(MeterRegistry registry, DeviceService deviceService, AlertService alertService,
                         RabbitAdmin rabbitAdmin) {
        Gauge.builder("fleet.devices.online", deviceService, DeviceService::countOnline)
                .description("Devices currently online")
                .register(registry);
        Gauge.builder("fleet.alerts.active", alertService, AlertService::countActive)
                .description("Alerts currently active")
                .register(registry);
        for (String queue : List.of(RabbitConfig.QUEUE_PERSIST, RabbitConfig.QUEUE_ALERT,
                RabbitConfig.QUEUE_LIVE, RabbitConfig.QUEUE_DLQ)) {
            Gauge.builder("fleet.queue.depth", () -> queueDepth(rabbitAdmin, queue))
                    .tag("queue", queue)
                    .description("Messages ready in the queue")
                    .register(registry);
        }
    }

    private static Number queueDepth(RabbitAdmin rabbitAdmin, String queue) {
        try {
            QueueInformation info = rabbitAdmin.getQueueInfo(queue);
            return info == null ? 0 : info.getMessageCount();
        } catch (Exception ex) {
            // Broker unreachable: report NaN so the sample is skipped rather than failing the whole scrape.
            return Double.NaN;
        }
    }
}
