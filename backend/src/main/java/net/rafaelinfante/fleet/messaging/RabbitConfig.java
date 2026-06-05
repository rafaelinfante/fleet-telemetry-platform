package net.rafaelinfante.fleet.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * RabbitMQ topology. One publish onto the topic exchange fans out to three independent work
 * queues; each has a dead-letter route so poison messages park on the shared DLQ instead of
 * being retried forever. Queues are quorum (replicated) and carry an explicit delivery limit —
 * without a DLX a quorum queue would silently drop a message that hits that limit.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "fleet.telemetry";
    public static final String DLX = "fleet.telemetry.dlx";

    public static final String QUEUE_PERSIST = "fleet.telemetry.persist";
    public static final String QUEUE_ALERT = "fleet.telemetry.alert";
    public static final String QUEUE_LIVE = "fleet.telemetry.live";
    public static final String QUEUE_DLQ = "fleet.telemetry.dlq";

    /** Routing key for a reading event: {@code telemetry.reading.<deviceId>}. */
    public static final String RK_READING_PREFIX = "telemetry.reading.";
    /** Routing key for a status event: {@code telemetry.status.<deviceId>}. */
    public static final String RK_STATUS_PREFIX = "telemetry.status.";

    private static final int DELIVERY_LIMIT = 5;

    @Bean
    RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    TopicExchange telemetryExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    TopicExchange telemetryDlx() {
        return ExchangeBuilder.topicExchange(DLX).durable(true).build();
    }

    @Bean
    Queue persistQueue() {
        return workQueue(QUEUE_PERSIST, "persist");
    }

    @Bean
    Queue alertQueue() {
        return workQueue(QUEUE_ALERT, "alert");
    }

    @Bean
    Queue liveQueue() {
        return workQueue(QUEUE_LIVE, "live");
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(QUEUE_DLQ).quorum().build();
    }

    private static Queue workQueue(String name, String deadLetterKey) {
        return QueueBuilder.durable(name)
                .quorum()
                .deliveryLimit(DELIVERY_LIMIT)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(deadLetterKey)
                .build();
    }

    // Persist only cares about readings; alert and live see readings and status transitions.
    @Bean
    Binding persistBinding() {
        return BindingBuilder.bind(persistQueue()).to(telemetryExchange()).with("telemetry.reading.#");
    }

    @Bean
    Binding alertBinding() {
        return BindingBuilder.bind(alertQueue()).to(telemetryExchange()).with("telemetry.#");
    }

    @Bean
    Binding liveBinding() {
        return BindingBuilder.bind(liveQueue()).to(telemetryExchange()).with("telemetry.#");
    }

    @Bean
    Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(telemetryDlx()).with("#");
    }

    /**
     * Single converter shared by the auto-configured RabbitTemplate and listener factory. It
     * trusts only this application's own event package when resolving the {@code __TypeId__} header.
     */
    @Bean
    MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("net.rafaelinfante.fleet.messaging");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}
