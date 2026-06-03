package net.rafaelinfante.fleet.alerting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.rafaelinfante.fleet.domain.Alert;
import net.rafaelinfante.fleet.domain.AlertRepository;
import net.rafaelinfante.fleet.domain.AlertRule;
import net.rafaelinfante.fleet.domain.AlertSeverity;
import net.rafaelinfante.fleet.domain.AlertStatus;
import net.rafaelinfante.fleet.domain.AlertType;
import net.rafaelinfante.fleet.observability.FleetMetrics;
import net.rafaelinfante.fleet.web.TelemetryBroadcaster;
import net.rafaelinfante.fleet.web.mapper.AlertMapper;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository repository;
    @Mock
    private FleetMetrics metrics;
    @Mock
    private TelemetryBroadcaster broadcaster;
    @Mock
    private AlertMapper mapper;
    @InjectMocks
    private AlertService service;

    private static AlertRule thresholdRule() {
        AlertRule rule = new AlertRule();
        rule.setId(1L);
        rule.setType(AlertType.THRESHOLD);
        rule.setSeverity(AlertSeverity.WARNING);
        rule.setThreshold(120.0);
        return rule;
    }

    @Test
    void acknowledgedAlertStillSuppressesDuplicate() {
        Alert acknowledged = new Alert();
        acknowledged.setStatus(AlertStatus.ACKNOWLEDGED);
        when(repository.findFirstByDeviceIdAndRuleIdAndStatusIn(eq("VH-1"), eq(1L), anyCollection()))
                .thenReturn(Optional.of(acknowledged));

        service.raiseForRule("VH-1", thresholdRule(), 150.0, "overspeed");

        verify(repository, never()).save(any());
    }

    @Test
    void resolvesAnAcknowledgedAlertWhenConditionClears() {
        Alert acknowledged = new Alert();
        acknowledged.setStatus(AlertStatus.ACKNOWLEDGED);
        when(repository.findFirstByDeviceIdAndRuleIdAndStatusIn(eq("VH-1"), eq(1L), anyCollection()))
                .thenReturn(Optional.of(acknowledged));

        service.resolveForRule("VH-1", 1L);

        assertThat(acknowledged.getStatus()).isEqualTo(AlertStatus.RESOLVED);
        assertThat(acknowledged.getResolvedAt()).isNotNull();
        verify(repository).save(acknowledged);
    }
}
