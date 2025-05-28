package swd392.eventmanagement.service;

import swd392.eventmanagement.model.dto.request.DepartmentRequest;
import swd392.eventmanagement.model.dto.response.DepartmentResponse;
import swd392.eventmanagement.model.entity.Department;

public interface DepartmentService {
    void createDepartment(DepartmentRequest requestDTO);
}

