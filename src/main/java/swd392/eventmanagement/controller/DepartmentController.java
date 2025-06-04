package swd392.eventmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

   @PostMapping("")
   @PreAuthorize("hasRole('ROLE_ADMIN')")
   @Operation(
           summary = "Create a new department",
           description = "Create a new department with unique code and name",
           security = @SecurityRequirement(name = "bearerAuth")
   )
   @ApiResponse(responseCode = "201",description = "Department created successfully",content = @Content(schema = @Schema(implementation = DepartmentResponse.class)))
   @ApiResponse(responseCode = "400",description = "Bad Request-Invalid department data supplied")
   @ApiResponse(responseCode = "403",description = "Forbidden-User does not have permission")
   @ApiResponse(responseCode = "409",description = "Conflict-Department code or name already exists")
   public ResponseEntity<DepartmentResponse> createDepartment(@Valid @RequestBody DepartmentRequest requestDTO) {
       return ResponseEntity
               .status(HttpStatus.CREATED)
               .body(departmentService.createDepartment(requestDTO));
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
    @PreAuthorize("hasRole('ROLE_LECTURER') or hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Update department",
            description = "Updates department information by ID",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Department updated successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid department data"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Department not found"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have permission"
            )
    })
    public ResponseEntity<Map<String, String>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request
    ) {
        departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(Map.of("message", "Department updated successfully"));
    }




    @GetMapping("/detail/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Get department detail",
            description = "Retrieve detailed information of a department by its ID",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved department details",
                    content = @Content(schema = @Schema(implementation = DepartmentResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Access is denied"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<DepartmentResponse> getDepartmentDetail(@PathVariable Long id) {
        DepartmentResponse dto = departmentService.getDepartmentDetailByCode(id);
        return ResponseEntity.ok(dto);
    }

}
