package swd392.eventmanagement.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swd392.eventmanagement.exception.DepartmentNotFoundException;
import swd392.eventmanagement.exception.DepartmentProcessingException;
import swd392.eventmanagement.exception.ValidationException;
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
        logger.info("Creating new department with code: {}", requestDTO.getCode());

        try {
            // 1. Check for duplicate department code
            if (departmentRepository.existsByCode(requestDTO.getCode())) {
                throw new ValidationException("Department code already exists");
            }

            // 2. Check for duplicate department name
            if (departmentRepository.existsByName(requestDTO.getName())) {
                throw new ValidationException("Department name already exists");
            }

            // 3. Map from DTO to entity
            Department department = departmentMapper.toEntity(requestDTO);

            // 4. Save entity and retrieve saved entity with ID
            Department savedDepartment = departmentRepository.save(department);

            // 5. Map saved entity to response DTO
            DepartmentResponse response = departmentMapper.toResponse(savedDepartment);

            logger.info("Department created successfully - ID: {}, Code: {}, Name: {}",
                    savedDepartment.getId(), savedDepartment.getCode(), savedDepartment.getName());

            return response;
        } catch (ValidationException e) {
            logger.warn("Validation error while creating department: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error occurred while creating department", e);
            throw new DepartmentProcessingException("Failed to create department", e);
        }
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
        logger.info("Updating department ID: {} with new code : {}", id, request.getCode());

        try {
            Department department = departmentRepository.findById(id)
                    .orElseThrow(() -> new DepartmentNotFoundException("Department not found with ID = " + id));

            //Check if new code already exists for another department
            if (!department.getCode().equals(request.getCode()) &&
                    departmentRepository.existsByCode(request.getCode())) {
                throw new ValidationException("Department code already exists");
            }

            //Check if the new name already exists for another department
            if (!department.getName().equals(request.getName()) &&
                    departmentRepository.existsByName(request.getName())) {
                throw new ValidationException("Department name already exists");
            }

            //Update fields
            department.setName(request.getName());
            department.setCode(request.getCode());
            department.setAvatarUrl(request.getAvatarUrl());
            department.setDescription(request.getDescription());
            department.setBannerUrl(request.getBannerUrl());

            departmentRepository.save(department);

            logger.info("Department updated successfully - ID: {}, Code: {}, Name: {}",
                    department.getId(), department.getCode(), department.getName());

        } catch (ValidationException e) {
            logger.warn("Validation error while updating department ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error occurred while updating department ID: {}", id, e);
            throw new DepartmentProcessingException("Failed to update department", e);
        }

    }


    @Override
    public DepartmentResponse getDepartmentDetailByCode(Long id) {
        logger.info("Fetching department details by ID: {}", id);
        try {
            Department department = departmentRepository.findById(id)
                    .orElseThrow(() -> new DepartmentNotFoundException("Department not found with ID = " + id));

            DepartmentResponse dto = new DepartmentResponse();
            dto.setId(department.getId());
            dto.setName(department.getName());
            dto.setCode(department.getCode());
            dto.setDescription(department.getDescription());
            dto.setAvatarUrl(department.getAvatarUrl());
            dto.setBannerUrl(department.getBannerUrl());
            dto.setCreatedAt(department.getCreatedAt().toString());
            dto.setUpdatedAt(department.getUpdatedAt().toString());

            logger.info("Fetched department details successfully for ID: {}", id);
            return dto;
        } catch (DepartmentNotFoundException e) {
            logger.warn("Department not found for ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while fetching department details for ID: {}", id, e);
            throw new DepartmentProcessingException("Failed to fetch department details", e);
        }

    }
}
