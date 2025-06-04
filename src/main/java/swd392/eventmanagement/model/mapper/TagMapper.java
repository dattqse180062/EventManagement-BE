package swd392.eventmanagement.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import swd392.eventmanagement.model.dto.response.TagShowDTO;
import swd392.eventmanagement.model.entity.Tag;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface TagMapper {
    TagMapper INSTANCE = Mappers.getMapper(TagMapper.class);

    TagShowDTO toDTO(Tag tag);

    List<TagShowDTO> toListDTOs(List<Tag> tags);

    Set<TagShowDTO> toDTOs(Set<Tag> tags);
}
