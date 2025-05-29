package swd392.eventmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import swd392.eventmanagement.model.dto.request.DepartmentRequest;
import swd392.eventmanagement.model.dto.response.DepartmentShowDTO;
import swd392.eventmanagement.service.impl.DepartmentServiceImpl;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
@Tag(name = "Department", description = "Department API")
public class DepartmentController {

    private final DepartmentServiceImpl departmentService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createDepartment(@RequestBody DepartmentRequest requestDTO) {
        departmentService.createDepartment(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Tạo phòng ban thành công"));
    }

    @GetMapping
    @Operation(summary = "Get all departments", description = "Returns a list of all departments in the system", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Departments retrieved successfully", content = @Content(schema = @Schema(implementation = DepartmentShowDTO.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission")
    @ApiResponse(responseCode = "404", description = "Not Found - No departments found in the system")
    public ResponseEntity<?> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }
}
