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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import swd392.eventmanagement.model.dto.request.AssignRoleRequest;
import swd392.eventmanagement.service.impl.UserDepartmentRoleServiceImpl;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/userdepartmentrole")
@RequiredArgsConstructor
@Tag(name = "User Department Role", description = "User Department Role API")
public class UserDepartmentRoleController {

    private final UserDepartmentRoleServiceImpl userDepartmentRoleService;

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
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
}
