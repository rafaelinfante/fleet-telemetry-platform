package net.rafaelinfante.fleet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fleet.mqtt")
public record MqttProperties(
        String url,
        String clientId,
        String telemetryTopic,
        String statusTopic,
        int qos) {
}
