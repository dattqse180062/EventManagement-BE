package swd392.eventmanagement.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import swd392.eventmanagement.model.dto.request.DepartmentRequest;
import swd392.eventmanagement.model.dto.response.DepartmentResponse;
import swd392.eventmanagement.model.entity.Department;

@Mapper(componentModel = "Spring")
public interface DepartmentMapper {

    DepartmentMapper INSTANCE = Mappers.getMapper(DepartmentMapper.class);

    //Mapping Từ Request -> Entity
    Department toEntity(DepartmentRequest departmentRequest );

    //Mapping tu Entity ->Response
    @Mapping(target = "createdAt", expression = "java(department.getCreatedAt().toString())")
    @Mapping(target = "updatedAt", expression = "java(department.getUpdatedAt().toString())")
    DepartmentResponse toResponse(Department department);
}
