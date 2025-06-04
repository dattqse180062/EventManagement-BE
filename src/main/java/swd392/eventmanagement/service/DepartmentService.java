package swd392.eventmanagement.service;

import java.util.List;

import swd392.eventmanagement.model.dto.request.DepartmentRequest;
import swd392.eventmanagement.model.dto.response.DepartmentResponse;
import swd392.eventmanagement.model.dto.response.DepartmentShowDTO;


public interface DepartmentService {
    DepartmentResponse createDepartment(DepartmentRequest requestDTO);

    List<DepartmentShowDTO> getAllDepartments();

    void updateDepartment(Long id, DepartmentRequest request);

    DepartmentResponse getDepartmentDetailByCode(Long id);
}
