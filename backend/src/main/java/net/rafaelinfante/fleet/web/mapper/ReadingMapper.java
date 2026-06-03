package net.rafaelinfante.fleet.web.mapper;

import org.mapstruct.Mapper;

import net.rafaelinfante.fleet.domain.TelemetryReading;
import net.rafaelinfante.fleet.web.dto.ReadingView;

@Mapper(componentModel = "spring")
public interface ReadingMapper {

    ReadingView toView(TelemetryReading reading);
}
