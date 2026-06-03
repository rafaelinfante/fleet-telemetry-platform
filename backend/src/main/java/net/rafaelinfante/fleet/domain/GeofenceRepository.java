package net.rafaelinfante.fleet.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GeofenceRepository extends JpaRepository<Geofence, Long> {
}
