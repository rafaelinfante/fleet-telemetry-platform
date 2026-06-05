package net.rafaelinfante.fleet.messaging;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.rafaelinfante.fleet.observability.FleetMetrics;

/**
 * Drains the dead-letter queue so poison messages are observable rather than silently lost.
 * The broker's {@code x-death} header records which work queue rejected the message and why.
 */
@Component
@Slf4j
public class DeadLetterConsumer {

    private static final int MAX_LOGGED_BODY = 500;

    private final FleetMetrics metrics;

    public DeadLetterConsumer(FleetMetrics metrics) {
        this.metrics = metrics;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_DLQ)
    public void onDeadLetter(Message message) {
        MessageProperties props = message.getMessageProperties();
        String sourceQueue = firstDeathAttribute(props, "queue");
        String reason = firstDeathAttribute(props, "reason");
        metrics.deadLettered(sourceQueue);
        log.warn("Dead-lettered message from '{}' (reason={}): {}",
                sourceQueue, reason, truncate(new String(message.getBody(), StandardCharsets.UTF_8)));
    }

    @SuppressWarnings("unchecked")
    private static String firstDeathAttribute(MessageProperties props, String attribute) {
        Object xDeath = props.getHeader("x-death");
        if (xDeath instanceof List<?> deaths && !deaths.isEmpty()
                && deaths.get(0) instanceof Map<?, ?> first) {
            Object value = ((Map<String, ?>) first).get(attribute);
            if (value != null) {
                return value.toString();
            }
        }
        return "unknown";
    }

    private static String truncate(String body) {
        return body.length() <= MAX_LOGGED_BODY ? body : body.substring(0, MAX_LOGGED_BODY) + "...";
    }
}
