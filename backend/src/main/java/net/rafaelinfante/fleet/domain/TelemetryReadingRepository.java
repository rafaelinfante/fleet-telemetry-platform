package net.rafaelinfante.fleet.domain;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelemetryReadingRepository extends JpaRepository<TelemetryReading, Long> {

    boolean existsByDeviceIdAndRecordedAt(String deviceId, Instant recordedAt);

    Page<TelemetryReading> findByDeviceId(String deviceId, Pageable pageable);

    Page<TelemetryReading> findByDeviceIdAndRecordedAtBetween(
            String deviceId, Instant from, Instant to, Pageable pageable);
}
