package net.rafaelinfante.fleet.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import net.rafaelinfante.fleet.alerting.AlertEngine;

/**
 * Runs the rule engine over each event. Single consumer so alert state (raise/resolve/dedup)
 * is evaluated sequentially without contention.
 */
@Component
public class AlertConsumer {

    private final AlertEngine engine;

    public AlertConsumer(AlertEngine engine) {
        this.engine = engine;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_ALERT, concurrency = "1")
    public void onEvent(TelemetryEvent event) {
        switch (event.eventType()) {
            case READING -> {
                if (ReadingValidator.isValidReading(event)) {
                    engine.evaluateReading(event);
                }
            }
            case STATUS -> engine.evaluateStatus(event);
        }
    }
}
