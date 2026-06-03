package net.rafaelinfante.fleet.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.rafaelinfante.fleet.domain.Device;
import net.rafaelinfante.fleet.domain.DeviceRepository;
import net.rafaelinfante.fleet.domain.DeviceStatus;
import net.rafaelinfante.fleet.domain.DeviceType;
import net.rafaelinfante.fleet.messaging.TelemetryEvent;

/**
 * Owns the device row and its rolling live snapshot. Devices auto-register on first contact;
 * a first-sight race between concurrent consumers surfaces as a constraint violation, which the
 * caller's message is simply redelivered for (by then the row exists).
 */
@Service
public class DeviceService {

    private final DeviceRepository repository;

    public DeviceService(DeviceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Device applyReading(TelemetryEvent event) {
        Device device = repository.findById(event.deviceId())
                .orElseGet(() -> newDevice(event.deviceId(), event.deviceType(), event.recordedAt()));
        device.setStatus(DeviceStatus.ONLINE);
        device.setLastSeenAt(event.recordedAt());
        device.setLastLatitude(event.latitude());
        device.setLastLongitude(event.longitude());
        device.setLastSpeedKmh(event.speedKmh());
        device.setLastBatteryPct(event.batteryPct());
        device.setLastFuelPct(event.fuelPct());
        device.setLastEngineTempC(event.engineTempC());
        device.setLastOdometerKm(event.odometerKm());
        if (event.deviceType() != null) {
            device.setType(event.deviceType());
        }
        return repository.save(device);
    }

    @Transactional
    public Device applyStatus(String deviceId, DeviceStatus status, Instant at) {
        Device device = repository.findById(deviceId)
                .orElseGet(() -> newDevice(deviceId, null, at));
        device.setStatus(status);
        if (status == DeviceStatus.ONLINE) {
            device.setLastSeenAt(at);
        }
        return repository.save(device);
    }

    @Transactional
    public List<Device> markStaleDevicesOffline(Instant cutoff) {
        List<Device> stale = repository.findByStatusAndLastSeenAtBefore(DeviceStatus.ONLINE, cutoff);
        stale.forEach(device -> device.setStatus(DeviceStatus.OFFLINE));
        return repository.saveAll(stale);
    }

    @Transactional(readOnly = true)
    public long countOnline() {
        return repository.countByStatus(DeviceStatus.ONLINE);
    }

    private static Device newDevice(String deviceId, DeviceType type, Instant firstSeenAt) {
        return new Device(deviceId, deviceId, type != null ? type : DeviceType.CAR, firstSeenAt);
    }
}
