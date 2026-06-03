package net.rafaelinfante.fleet.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP over WebSocket for pushing live device state and alerts to the dashboard. The in-memory
 * simple broker is enough at this scale; the heartbeat scheduler keeps idle connections alive.
 * Origins are open for the demo — locking these down belongs with API/WS authentication on the roadmap.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(org.springframework.messaging.simp.config.MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[]{10_000, 10_000})
                .setTaskScheduler(brokerHeartbeatScheduler());
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Bean
    ThreadPoolTaskScheduler brokerHeartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        return scheduler;
    }
}
