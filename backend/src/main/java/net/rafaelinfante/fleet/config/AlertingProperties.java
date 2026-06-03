package net.rafaelinfante.fleet.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fleet.alerting")
public record AlertingProperties(
        Duration offlineThreshold,
        Duration offlineCheckInterval) {
}
