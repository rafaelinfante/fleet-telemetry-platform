package net.rafaelinfante.fleet.messaging;

import java.util.Map;

import org.springframework.boot.autoconfigure.amqp.RabbitRetryTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.policy.SimpleRetryPolicy;

/**
 * Distinguishes poison messages from transient failures. A {@link CorruptReadingException} can never
 * succeed, so it is not retried and goes straight to the dead-letter queue; any other failure (a brief
 * DB or broker hiccup) is retried with back-off and only dead-letters if it is still failing after the
 * whole window, so a momentary blip never discards valid telemetry.
 */
@Configuration
public class RabbitRetryConfig {

    static final int MAX_ATTEMPTS = 5;

    @Bean
    RabbitRetryTemplateCustomizer listenerRetryClassifier() {
        return (target, retryTemplate) -> {
            if (target != RabbitRetryTemplateCustomizer.Target.LISTENER) {
                return;
            }
            Map<Class<? extends Throwable>, Boolean> retryable = Map.of(CorruptReadingException.class, false);
            // traverseCauses=true: the listener wraps the real cause; defaultValue=true: retry everything else.
            retryTemplate.setRetryPolicy(new SimpleRetryPolicy(MAX_ATTEMPTS, retryable, true, true));
        };
    }
}
