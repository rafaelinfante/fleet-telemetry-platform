package net.rafaelinfante.fleet.web;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.rafaelinfante.fleet.alerting.AlertService;
import net.rafaelinfante.fleet.domain.AlertStatus;
import net.rafaelinfante.fleet.web.dto.AlertView;
import net.rafaelinfante.fleet.web.dto.PageResponse;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public PageResponse<AlertView> list(
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) String deviceId,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return alertService.search(status, deviceId, pageable);
    }

    @PostMapping("/{id}/acknowledge")
    public AlertView acknowledge(@PathVariable Long id) {
        return alertService.acknowledge(id);
    }

    @PostMapping("/{id}/resolve")
    public AlertView resolve(@PathVariable Long id) {
        return alertService.resolve(id);
    }
}
