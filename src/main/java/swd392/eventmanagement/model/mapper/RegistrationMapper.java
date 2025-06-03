package swd392.eventmanagement.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import swd392.eventmanagement.model.dto.response.RegistrationCancelResponse;
import swd392.eventmanagement.model.dto.response.RegistrationCreateResponse;
import swd392.eventmanagement.model.entity.Registration;

@Mapper(componentModel = "spring")
public interface RegistrationMapper {
    RegistrationMapper INSTANCE = Mappers.getMapper(RegistrationMapper.class);

    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "name", source = "user.fullName")
    @Mapping(target = "eventName", source = "event.name")
    @Mapping(target = "createdAt", expression = "java(registration.getCreatedAt().toString())")
    RegistrationCreateResponse toRegistrationCreateResponse(Registration registration);

    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "name", source = "user.fullName")
    @Mapping(target = "eventName", source = "event.name")
    @Mapping(target = "cancelledAt", source = "canceledAt")
    RegistrationCancelResponse toRegistrationCancelResponse(Registration registration);
}
