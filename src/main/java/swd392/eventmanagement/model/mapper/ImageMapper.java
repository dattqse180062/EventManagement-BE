package swd392.eventmanagement.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import swd392.eventmanagement.model.dto.response.ImageDTO;
import swd392.eventmanagement.model.entity.Image;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    ImageMapper INSTANCE = Mappers.getMapper(ImageMapper.class);

    ImageDTO toDTO(Image image);

    Set<ImageDTO> toDTOSet(Set<Image> images);
}
