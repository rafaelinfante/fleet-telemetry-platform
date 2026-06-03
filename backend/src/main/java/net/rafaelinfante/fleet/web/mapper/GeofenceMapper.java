package net.rafaelinfante.fleet.web.mapper;

import org.mapstruct.Mapper;

import net.rafaelinfante.fleet.domain.Geofence;
import net.rafaelinfante.fleet.web.dto.GeofenceView;

@Mapper(componentModel = "spring")
public interface GeofenceMapper {

    GeofenceView toView(Geofence geofence);
}
