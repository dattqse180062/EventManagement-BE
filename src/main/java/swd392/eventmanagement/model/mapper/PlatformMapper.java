package swd392.eventmanagement.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import swd392.eventmanagement.model.dto.response.PlatformDTO;
import swd392.eventmanagement.model.entity.Platform;

@Mapper(componentModel = "spring")
public interface PlatformMapper {
    PlatformMapper INSTANCE = Mappers.getMapper(PlatformMapper.class);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "url", source = "url")
    PlatformDTO toDTO(Platform platform);
}
