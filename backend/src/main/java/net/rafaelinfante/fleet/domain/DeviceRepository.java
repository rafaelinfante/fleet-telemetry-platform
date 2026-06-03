package net.rafaelinfante.fleet.domain;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, String> {

    long countByStatus(DeviceStatus status);

    List<Device> findByStatusAndLastSeenAtBefore(DeviceStatus status, Instant cutoff);
}
