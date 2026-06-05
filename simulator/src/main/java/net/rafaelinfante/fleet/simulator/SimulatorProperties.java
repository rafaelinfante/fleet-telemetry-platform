package net.rafaelinfante.fleet.simulator;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "simulator")
public record SimulatorProperties(
        String brokerUrl,
        int deviceCount,
        Duration publishInterval,
        double anomalyRate,
        double corruptRate,
        double centerLat,
        double centerLng,
        double spreadKm) {
}
