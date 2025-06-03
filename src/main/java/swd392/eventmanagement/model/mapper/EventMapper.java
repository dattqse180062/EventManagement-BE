package swd392.eventmanagement.model.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import swd392.eventmanagement.enums.EventStatus;
import swd392.eventmanagement.model.dto.response.EventDetailsDTO;
import swd392.eventmanagement.model.dto.response.EventDetailsManagementDTO;
import swd392.eventmanagement.model.dto.response.EventListDTO;
import swd392.eventmanagement.model.dto.response.EventListManagementDTO;
import swd392.eventmanagement.model.dto.response.EventUpdateStatusResponse;
import swd392.eventmanagement.model.entity.Department;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.EventType;
import swd392.eventmanagement.model.entity.Location;

@Mapper(componentModel = "spring", uses = { TagMapper.class, ImageMapper.class, LocationMapper.class,
        PlatformMapper.class })
public interface EventMapper {
    EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);

    @Mapping(target = "departmentName", source = "department", qualifiedByName = "departmentToName")
    @Mapping(target = "typeName", source = "type", qualifiedByName = "eventTypeToName")
    @Mapping(target = "locationAddress", source = "location", qualifiedByName = "locationToAddress")
    @Mapping(target = "locationAddress2", source = "location", qualifiedByName = "locationToAddress2")
    @Mapping(target = "tags", source = "tags")
    EventListDTO toDTO(Event event);

    @Mapping(target = "typeName", source = "type", qualifiedByName = "eventTypeToName")
    @Mapping(target = "locationAddress", source = "location", qualifiedByName = "locationToAddress")
    EventListManagementDTO toEventListManagementDTO(Event event);

    List<EventListManagementDTO> toEventListManagementDTOList(List<Event> events);

    List<EventListDTO> toDTOList(List<Event> events);

    @Mapping(target = "departmentName", source = "department", qualifiedByName = "departmentToName")
    @Mapping(target = "departmentAvatarUrl", source = "department.avatarUrl")
    @Mapping(target = "typeName", source = "type", qualifiedByName = "eventTypeToName")
    @Mapping(target = "locationAddress", source = "location", qualifiedByName = "locationToAddress")
    @Mapping(target = "locationAddress2", source = "location", qualifiedByName = "locationToAddress2")
    @Mapping(target = "platformName", source = "platform.name")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "images", source = "images")
    @Mapping(target = "registeredCount", ignore = true)
    @Mapping(target = "maxCapacityStudent", ignore = true)
    @Mapping(target = "registeredCountStudent", ignore = true)
    @Mapping(target = "maxCapacityLecturer", ignore = true)
    @Mapping(target = "registeredCountLecturer", ignore = true)
    @Mapping(target = "isRegistered", ignore = true)
    @Mapping(target = "registrationStatus", ignore = true)
    EventDetailsDTO toEventDetailsDTO(Event event);

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "currentStatus", source = "event.status")
    @Mapping(target = "previousStatus", source = "previousStatus")
    EventUpdateStatusResponse toEventUpdateStatusResponse(Event event, EventStatus previousStatus);

    @Named("departmentToName")
    default String departmentToName(Department department) {
        return department != null ? department.getName() : null;
    }

    @Named("eventTypeToName")
    default String eventTypeToName(EventType eventType) {
        return eventType != null ? eventType.getName() : null;
    }

    @Named("locationToAddress")
    default String locationToAddress(Location location) {
        return location != null ? location.getAddress() : null;
    }

    @Named("locationToAddress2")
    default String locationToFullAddress(Location location) {
        if (location == null)
            return null;
        String ward = location.getWard();
        String district = location.getDistrict();
        String city = location.getCity();
        StringBuilder sb = new StringBuilder();
        if (ward != null && !ward.isEmpty())
            sb.append(ward);
        if (district != null && !district.isEmpty()) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(district);
        }
        if (city != null && !city.isEmpty()) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(city);
        }
        return sb.toString();
    }

    @Mapping(target = "typeId", source = "type.id")
    @Mapping(target = "typeName", source = "type.name")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "platform", source = "platform")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "images", source = "images")
    @Mapping(target = "surveyId", source = "survey.id")
    @Mapping(target = "maxCapacityStudent", ignore = true)
    @Mapping(target = "maxCapacityLecturer", ignore = true)
    EventDetailsManagementDTO toEventDetailsManagement(Event event);
}
