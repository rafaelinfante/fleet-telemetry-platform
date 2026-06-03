package net.rafaelinfante.fleet.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import net.rafaelinfante.fleet.service.GeofenceService;
import net.rafaelinfante.fleet.web.dto.GeofenceRequest;
import net.rafaelinfante.fleet.web.dto.GeofenceView;

@RestController
@RequestMapping("/api/geofences")
public class GeofenceController {

    private final GeofenceService service;

    public GeofenceController(GeofenceService service) {
        this.service = service;
    }

    @GetMapping
    public List<GeofenceView> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public GeofenceView get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GeofenceView create(@Valid @RequestBody GeofenceRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public GeofenceView update(@PathVariable Long id, @Valid @RequestBody GeofenceRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
