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
import org.springframework.web.bind.annotation.*;
import swd392.eventmanagement.model.dto.request.DepartmentRequest;
import swd392.eventmanagement.model.dto.response.DepartmentResponse;
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

    @PutMapping("/update/{id}")
    @Operation(
            summary = "Update department",
            description = "Cập nhật thông tin phòng ban",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(
            responseCode = "200",
            description = "Cập nhật phòng ban thành công",
            content = @Content(schema = @Schema(implementation = Map.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy phòng ban"
    )
    public ResponseEntity<Map<String, String>> updateDepartment(
            @PathVariable Long id,
            @RequestBody DepartmentRequest request
    ) {
        departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(Map.of("message", "Cập nhật phòng ban thành công"));
    }

    @GetMapping("/detail/{id}")
    @Operation(
            summary = "Get department detail",
            description = "Lấy thông tin chi tiết phòng ban theo ID",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lấy thông tin phòng ban thành công",
            content = @Content(schema = @Schema(implementation = DepartmentResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy phòng ban"
    )
    public ResponseEntity<DepartmentResponse> getDepartmentDetail(
            @PathVariable Long id
    ) {
        DepartmentResponse dto = departmentService.getDepartmentDetailByCode(id);
        return ResponseEntity.ok(dto);
    }

}
