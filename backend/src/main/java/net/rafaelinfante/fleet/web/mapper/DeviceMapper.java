package net.rafaelinfante.fleet.web.mapper;

import org.mapstruct.Mapper;

import net.rafaelinfante.fleet.domain.Device;
import net.rafaelinfante.fleet.web.dto.DeviceView;

@Mapper(componentModel = "spring")
public interface DeviceMapper {

    DeviceView toView(Device device);
}
