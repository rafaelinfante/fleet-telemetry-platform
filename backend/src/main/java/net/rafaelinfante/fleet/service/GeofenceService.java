package net.rafaelinfante.fleet.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.rafaelinfante.fleet.domain.Geofence;
import net.rafaelinfante.fleet.domain.GeofenceRepository;
import net.rafaelinfante.fleet.web.dto.GeofenceRequest;
import net.rafaelinfante.fleet.web.dto.GeofenceView;
import net.rafaelinfante.fleet.web.error.ResourceNotFoundException;
import net.rafaelinfante.fleet.web.mapper.GeofenceMapper;

@Service
public class GeofenceService {

    private final GeofenceRepository repository;
    private final GeofenceMapper mapper;

    public GeofenceService(GeofenceRepository repository, GeofenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<GeofenceView> findAll() {
        return repository.findAll().stream().map(mapper::toView).toList();
    }

    @Transactional(readOnly = true)
    public GeofenceView findById(Long id) {
        return mapper.toView(get(id));
    }

    @Transactional
    public GeofenceView create(GeofenceRequest request) {
        Geofence geofence = new Geofence();
        apply(geofence, request);
        geofence.setCreatedAt(Instant.now());
        return mapper.toView(repository.save(geofence));
    }

    @Transactional
    public GeofenceView update(Long id, GeofenceRequest request) {
        Geofence geofence = get(id);
        apply(geofence, request);
        return mapper.toView(repository.save(geofence));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Geofence %d not found".formatted(id));
        }
        repository.deleteById(id);
    }

    private static void apply(Geofence geofence, GeofenceRequest request) {
        geofence.setName(request.name());
        geofence.setCenterLat(request.centerLat());
        geofence.setCenterLng(request.centerLng());
        geofence.setRadiusM(request.radiusM());
    }

    private Geofence get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Geofence %d not found".formatted(id)));
    }
}
