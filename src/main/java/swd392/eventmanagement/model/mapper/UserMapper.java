package swd392.eventmanagement.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import swd392.eventmanagement.model.dto.response.UserDTO;
import swd392.eventmanagement.model.entity.User;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "roles", expression = "java(user.getRoles().stream().map(role -> role.getName()).collect(java.util.stream.Collectors.toSet()))")
    UserDTO toUserDTO(User user);

    default List<UserDTO> toUserShowDTOList(List<User> users) {
        return users.stream().map(this::toUserDTO).collect(Collectors.toList());
    }
}
