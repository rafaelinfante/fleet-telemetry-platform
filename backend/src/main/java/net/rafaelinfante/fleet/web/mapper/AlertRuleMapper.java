package net.rafaelinfante.fleet.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import net.rafaelinfante.fleet.domain.AlertRule;
import net.rafaelinfante.fleet.web.dto.AlertRuleView;

@Mapper(componentModel = "spring")
public interface AlertRuleMapper {

    @Mapping(target = "geofenceId", source = "geofence.id")
    @Mapping(target = "geofenceName", source = "geofence.name")
    AlertRuleView toView(AlertRule rule);
}
