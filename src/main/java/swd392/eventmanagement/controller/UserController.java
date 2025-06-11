package swd392.eventmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import swd392.eventmanagement.model.dto.response.UserDTO;
import swd392.eventmanagement.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "User API")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user information", description = "Returns the information of the currently authenticated user", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "User information retrieved successfully", content = @Content(schema = @Schema(implementation = UserDTO.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission")
    public ResponseEntity<?> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @GetMapping("/users/unassigned")
    @Operation(
            summary = "Get all users not assigned to any department role",
            description = "Returns a list of users who have not been assigned any role in any department",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserDTO.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have permission"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Not Found - No users found without department roles"
    )
    public ResponseEntity<?> getUsersNotInAnyDepartmentRole() {
        return ResponseEntity.ok(userService.getUsersNotInDepartment());
    }


}