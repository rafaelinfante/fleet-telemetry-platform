package net.rafaelinfante.fleet.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

/**
 * Spins up a fleet of {@link SimulatedDevice}s and drives them on a fixed cadence. Each tick a
 * device either publishes, or (rarely) drops offline for a few ticks to exercise the platform's
 * offline detection and recovery.
 */
@Component
@Slf4j
public class FleetSimulator {

    private static final String[] TYPES = {"CAR", "VAN", "TRUCK", "BIKE"};
    private static final double OFFLINE_PROBABILITY = 0.01;

    private final SimulatorProperties properties;
    private final ObjectMapper objectMapper;
    private final List<SimulatedDevice> devices = new ArrayList<>();
    private final Random random = new Random();

    public FleetSimulator(SimulatorProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        for (int i = 0; i < properties.deviceCount(); i++) {
            String deviceId = "VH-%04d".formatted(i + 1);
            SimulatedDevice device = new SimulatedDevice(deviceId, TYPES[i % TYPES.length],
                    properties.centerLat(), properties.centerLng(), properties.spreadKm(),
                    properties.brokerUrl(), objectMapper, i + 1L);
            try {
                device.connect();
                devices.add(device);
            } catch (Exception ex) {
                log.error("Failed to connect device {}: {}", deviceId, ex.getMessage());
            }
        }
        log.info("Started {} simulated devices publishing to {}", devices.size(), properties.brokerUrl());
    }

    @Scheduled(fixedDelayString = "${simulator.publish-interval}")
    public void tick() {
        if (devices.isEmpty()) {
            return;
        }
        double intervalSeconds = properties.publishInterval().toMillis() / 1000.0;
        for (SimulatedDevice device : devices) {
            if (device.isOffline()) {
                device.tickOffline();
            } else if (random.nextDouble() < OFFLINE_PROBABILITY) {
                device.goOffline(8 + random.nextInt(5));
            } else {
                device.publishTick(intervalSeconds, properties.anomalyRate(), properties.corruptRate());
            }
        }
    }

    @PreDestroy
    public void stop() {
        devices.forEach(SimulatedDevice::shutdown);
    }
}
