package net.rafaelinfante.fleet.web;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.rafaelinfante.fleet.service.DeviceQueryService;
import net.rafaelinfante.fleet.web.dto.DeviceView;
import net.rafaelinfante.fleet.web.dto.PageResponse;
import net.rafaelinfante.fleet.web.dto.ReadingView;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceQueryService service;

    public DeviceController(DeviceQueryService service) {
        this.service = service;
    }

    @GetMapping
    public List<DeviceView> list() {
        return service.findAll();
    }

    @GetMapping("/{deviceId}")
    public DeviceView get(@PathVariable String deviceId) {
        return service.findById(deviceId);
    }

    @GetMapping("/{deviceId}/readings")
    public PageResponse<ReadingView> readings(
            @PathVariable String deviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 50, sort = "recordedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.readings(deviceId, from, to, pageable);
    }
}
