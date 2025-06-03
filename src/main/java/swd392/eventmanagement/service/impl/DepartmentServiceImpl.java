package swd392.eventmanagement.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swd392.eventmanagement.exception.DepartmentException;
import swd392.eventmanagement.exception.DepartmentNotFoundException;
import swd392.eventmanagement.exception.DepartmentProcessingException;
import swd392.eventmanagement.model.dto.request.DepartmentRequest;
import swd392.eventmanagement.model.dto.response.DepartmentResponse;
import swd392.eventmanagement.model.dto.response.DepartmentShowDTO;
import swd392.eventmanagement.model.entity.Department;
import swd392.eventmanagement.model.mapper.DepartmentMapper;
import swd392.eventmanagement.repository.DepartmentRepository;
import swd392.eventmanagement.service.DepartmentService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentServiceImpl.class);
    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    @Transactional
    @Override
    public DepartmentResponse createDepartment(DepartmentRequest requestDTO) {
        logger.info("Create new department with code: {} ", requestDTO.getCode());

        // 1. Validate duplicate code
        if (departmentRepository.existsByCode(requestDTO.getCode())) {
            throw new DepartmentException("Department code already exists");
        }

        // 2. Validate duplicate name
        if (departmentRepository.existsByName(requestDTO.getName())) {
            throw new DepartmentException("Department name already exists");
        }

        // 3. Map từ DTO sang entity
        Department department = departmentMapper.toEntity(requestDTO);

        // 4. Lưu entity và lấy lại entity đã lưu (đã có ID)
        Department savedDepartment = departmentRepository.save(department);

        // 5. Map entity đã lưu sang response DTO
        DepartmentResponse response = departmentMapper.toResponse(savedDepartment);

        logger.info("Successfully created department ID: {}, code: {}, name: {}",
                savedDepartment.getId(), savedDepartment.getCode(), savedDepartment.getName());

        return response;
    }

    @Override
    public List<DepartmentShowDTO> getAllDepartments() {
        logger.info("Getting all departments");
        try {
            List<Department> departments = departmentRepository.findAll();

            if (departments.isEmpty()) {
                logger.info("No departments found");
                throw new DepartmentNotFoundException("No departments found in the system");
            }

            logger.info("Found {} departments", departments.size());
            return departmentMapper.toDepartmentShowDTOList(departments);
        } catch (DepartmentNotFoundException e) {
            // Just rethrow DepartmentNotFoundException to be handled by the global
            // exception handler
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving departments", e);
            throw new DepartmentProcessingException("Failed to retrieve departments", e);
        }
    }

    @Override
    public void updateDepartment(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException("Không tìm thấy phòng ban với ID = " + id));

        department.setName(request.getName());
        department.setCode(request.getCode());
        department.setAvatarUrl(request.getAvatarUrl());
        department.setBannerUrl(request.getBannerUrl());

        departmentRepository.save(department);
    }

    @Override
    public DepartmentResponse getDepartmentDetailByCode(Long id) {
       Department department =departmentRepository.findById(id)
               .orElseThrow(() -> new DepartmentNotFoundException("Department not found"));
       DepartmentResponse dto = new DepartmentResponse();
       dto.setId(department.getId());
       dto.setName(department.getName());
       dto.setCode(department.getCode());
       dto.setAvatarUrl(department.getAvatarUrl());
       dto.setBannerUrl(department.getBannerUrl());
       dto.setCreatedAt(department.getCreatedAt().toString());
       dto.setUpdatedAt(department.getUpdatedAt().toString());
       return dto;

    }

}
