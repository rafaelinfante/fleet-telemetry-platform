package net.rafaelinfante.fleet.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.rafaelinfante.fleet.domain.DeviceRepository;
import net.rafaelinfante.fleet.domain.TelemetryReadingRepository;
import net.rafaelinfante.fleet.web.dto.DeviceView;
import net.rafaelinfante.fleet.web.dto.PageResponse;
import net.rafaelinfante.fleet.web.dto.ReadingView;
import net.rafaelinfante.fleet.web.error.ResourceNotFoundException;
import net.rafaelinfante.fleet.web.mapper.DeviceMapper;
import net.rafaelinfante.fleet.web.mapper.ReadingMapper;

@Service
public class DeviceQueryService {

    private final DeviceRepository deviceRepository;
    private final TelemetryReadingRepository readingRepository;
    private final DeviceMapper deviceMapper;
    private final ReadingMapper readingMapper;

    public DeviceQueryService(DeviceRepository deviceRepository, TelemetryReadingRepository readingRepository,
                              DeviceMapper deviceMapper, ReadingMapper readingMapper) {
        this.deviceRepository = deviceRepository;
        this.readingRepository = readingRepository;
        this.deviceMapper = deviceMapper;
        this.readingMapper = readingMapper;
    }

    @Transactional(readOnly = true)
    public List<DeviceView> findAll() {
        return deviceRepository.findAll().stream()
                .map(deviceMapper::toView)
                .sorted(Comparator.comparing(DeviceView::deviceId))
                .toList();
    }

    @Transactional(readOnly = true)
    public DeviceView findById(String deviceId) {
        return deviceMapper.toView(deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device %s not found".formatted(deviceId))));
    }

    @Transactional(readOnly = true)
    public PageResponse<ReadingView> readings(String deviceId, Instant from, Instant to, Pageable pageable) {
        if (!deviceRepository.existsById(deviceId)) {
            throw new ResourceNotFoundException("Device %s not found".formatted(deviceId));
        }
        var page = (from != null && to != null)
                ? readingRepository.findByDeviceIdAndRecordedAtBetween(deviceId, from, to, pageable)
                : readingRepository.findByDeviceId(deviceId, pageable);
        return PageResponse.of(page.map(readingMapper::toView));
    }
}
