package swd392.eventmanagement.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import swd392.eventmanagement.model.dto.request.DepartmentRequest;
import swd392.eventmanagement.model.entity.Department;
import swd392.eventmanagement.model.mapper.DepartmentMapper;
import swd392.eventmanagement.repository.DepartmentRepository;
import swd392.eventmanagement.service.DepartmentService;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    public void createDepartment(DepartmentRequest requestDTO) {
        if (departmentRepository.existsByCode(requestDTO.getCode())) {
            throw new RuntimeException("Mã phòng ban đã tồn tại");
        }
        if (departmentRepository.existsByName(requestDTO.getName())) {
            throw new RuntimeException("Tên phòng ban đã tồn tại");
        }

        Department department = departmentMapper.toEntity(requestDTO);
        departmentRepository.save(department);
    }
}
