package net.rafaelinfante.fleet.web.mapper;

import org.mapstruct.Mapper;

import net.rafaelinfante.fleet.domain.Alert;
import net.rafaelinfante.fleet.web.dto.AlertView;

@Mapper(componentModel = "spring")
public interface AlertMapper {

    AlertView toView(Alert alert);
}
