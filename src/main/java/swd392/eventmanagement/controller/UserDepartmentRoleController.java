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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import swd392.eventmanagement.model.dto.request.AssignRoleRequest;
import swd392.eventmanagement.model.dto.response.DepartmentRoleShowDTO;
import swd392.eventmanagement.service.impl.UserDepartmentRoleServiceImpl;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/userdepartmentrole")
@RequiredArgsConstructor
@Tag(name = "User Department Role", description = "User Department Role API")
public class UserDepartmentRoleController {

    private final UserDepartmentRoleServiceImpl userDepartmentRoleService;

    @PostMapping("")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Assign role to user in department",
            description = "Assign a role to a user within a department",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(
            responseCode = "201",
            description = "Role assigned successfully",
            content = @Content(schema = @Schema(implementation = Map.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Invalid input data"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have permission"
    )
    @ApiResponse(
            responseCode = "404",
            description = "User, department, or role not found"
    )
    public ResponseEntity<Map<String, String>> assignRole(@RequestBody AssignRoleRequest request) {
        userDepartmentRoleService.assignRole(
                request.getUserId(),
                request.getDepartmentId(),
                request.getDepartmentRoleId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Role assigned successfully"));
    }

    @GetMapping("")
    @Operation(
            summary = "Get all department roles",
            description = "Returns a list of all department roles in the system",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(
            responseCode = "200",
            description = "Department roles retrieved successfully",
            content = @Content(schema = @Schema(implementation = DepartmentRoleShowDTO.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have permission"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Not Found - No department roles found in the system"
    )
    public ResponseEntity<?> getAllDepartmentRoles() {
        return ResponseEntity.ok(userDepartmentRoleService.getAllDepartmentRoles());
    }
    @PutMapping("/update-role")
    @Operation(
            summary = "Update user's department role",
            description = "Updates the role of a user in a specific department",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(
            responseCode = "200",
            description = "User's department role updated successfully"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Validation failed"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have permission"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Not Found - User, Department, or Role not found"
    )
    public ResponseEntity<String> updateUserDepartmentRole(
            @RequestParam Long userId,
            @RequestParam Long departmentId,
            @RequestParam Long departmentRoleId
    ) {
        userDepartmentRoleService.updateUserDepartmentRole(userId, departmentId, departmentRoleId);
        return ResponseEntity.ok("User's department role updated successfully.");
    }

    @DeleteMapping("/remove-user-from-department")
    @Operation(
            summary = "Remove user from department",
            description = "Hard delete the relation between user and department",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(
            responseCode = "200",
            description = "User removed from department successfully"
    )
    @ApiResponse(
            responseCode = "404",
            description = "User or Department relation not found"
    )
    public ResponseEntity<?> removeUserFromDepartment(
            @RequestParam Long userId,
            @RequestParam Long departmentId
    ) {
        userDepartmentRoleService.removeUserFromDepartment(userId, departmentId);
        return ResponseEntity.ok("User removed from department successfully");
    }
}
