package net.rafaelinfante.fleet.alerting;

import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.rafaelinfante.fleet.config.AlertingProperties;
import net.rafaelinfante.fleet.domain.AlertRule;
import net.rafaelinfante.fleet.domain.Device;
import net.rafaelinfante.fleet.service.DeviceService;
import net.rafaelinfante.fleet.web.TelemetryBroadcaster;
import net.rafaelinfante.fleet.web.mapper.DeviceMapper;

/**
 * Safety net for offline detection. The last-will gives instant notice of an ungraceful
 * disconnect; this catches devices that simply went quiet without dropping their connection.
 */
@Component
public class OfflineDetectionJob {

    private final DeviceService deviceService;
    private final AlertEngine alertEngine;
    private final AlertService alertService;
    private final TelemetryBroadcaster broadcaster;
    private final DeviceMapper deviceMapper;
    private final AlertingProperties properties;

    public OfflineDetectionJob(DeviceService deviceService, AlertEngine alertEngine, AlertService alertService,
                               TelemetryBroadcaster broadcaster, DeviceMapper deviceMapper,
                               AlertingProperties properties) {
        this.deviceService = deviceService;
        this.alertEngine = alertEngine;
        this.alertService = alertService;
        this.broadcaster = broadcaster;
        this.deviceMapper = deviceMapper;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${fleet.alerting.offline-check-interval}")
    @Transactional
    public void detectOffline() {
        Instant cutoff = Instant.now().minus(properties.offlineThreshold());
        List<Device> nowOffline = deviceService.markStaleDevicesOffline(cutoff);
        if (nowOffline.isEmpty()) {
            return;
        }
        String message = "Device offline (no telemetry for over %ds)".formatted(properties.offlineThreshold().toSeconds());
        List<AlertRule> rules = alertEngine.enabledRules();
        nowOffline.forEach(device ->
                alertEngine.offlineRuleFor(rules, device.getType()).ifPresent(rule ->
                        alertService.raiseOffline(device.getDeviceId(), rule, message)));
        nowOffline.forEach(device -> broadcaster.broadcastDevice(deviceMapper.toView(device)));
    }
}
