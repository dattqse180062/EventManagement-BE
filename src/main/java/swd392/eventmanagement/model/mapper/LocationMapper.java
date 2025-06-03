package swd392.eventmanagement.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import swd392.eventmanagement.model.dto.response.LocationDTO;
import swd392.eventmanagement.model.entity.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationMapper INSTANCE = Mappers.getMapper(LocationMapper.class);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "ward", source = "ward")
    @Mapping(target = "district", source = "district")
    @Mapping(target = "city", source = "city")
    LocationDTO toDTO(Location location);
}
