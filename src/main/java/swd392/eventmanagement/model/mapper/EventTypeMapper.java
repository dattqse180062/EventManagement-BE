package swd392.eventmanagement.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import swd392.eventmanagement.model.dto.response.EventTypeDTO;
import swd392.eventmanagement.model.entity.EventType;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventTypeMapper {
    EventTypeMapper INSTANCE = Mappers.getMapper(EventTypeMapper.class);

    EventTypeDTO toDTO(EventType eventType);

    List<EventTypeDTO> toListDTO(List<EventType> eventTypes);
}
