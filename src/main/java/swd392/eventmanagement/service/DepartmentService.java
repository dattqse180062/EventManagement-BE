package swd392.eventmanagement.service;

import java.util.List;

import swd392.eventmanagement.model.dto.request.DepartmentRequest;
import swd392.eventmanagement.model.dto.response.DepartmentResponse;
import swd392.eventmanagement.model.dto.response.DepartmentShowDTO;
import swd392.eventmanagement.model.entity.Department;

public interface DepartmentService {
    void createDepartment(DepartmentRequest requestDTO);

    List<DepartmentShowDTO> getAllDepartments();
}
