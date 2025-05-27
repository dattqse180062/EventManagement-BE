package swd392.eventmanagement.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import swd392.eventmanagement.model.dto.response.TagShowDTO;
import swd392.eventmanagement.model.entity.Tag;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface TagMapper {
    TagMapper INSTANCE = Mappers.getMapper(TagMapper.class);

    TagShowDTO toDTO(Tag tag);

    Set<TagShowDTO> toDTOs(Set<Tag> tags);
}
