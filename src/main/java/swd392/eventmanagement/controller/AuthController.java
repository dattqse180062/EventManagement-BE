package swd392.eventmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import swd392.eventmanagement.model.dto.request.GoogleTokenRequest;
import swd392.eventmanagement.model.dto.request.TokenRefreshRequest;
import swd392.eventmanagement.model.dto.response.JwtResponse;
import swd392.eventmanagement.model.dto.response.TokenRefreshResponse;
import swd392.eventmanagement.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication API")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/google")
    @Operation(summary = "Authenticate with Google", description = "Authenticate using Google OAuth2 token")
    @ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(schema = @Schema(implementation = JwtResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid token or authentication failed")
    public ResponseEntity<?> authenticateWithGoogle(@RequestBody @Valid GoogleTokenRequest request) {
        try {
            return ResponseEntity.ok(authService.authenticateWithGoogle(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh the token")
    @ApiResponse(responseCode = "200", description = "Refresh token successful", content = @Content(schema = @Schema(implementation = TokenRefreshResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid token or authentication failed")
    public ResponseEntity<?> refreshToken(@RequestBody @Valid TokenRefreshRequest request) {
        try {
            return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout the user")
    @ApiResponse(responseCode = "200", description = "Logout successful")
    public ResponseEntity<?> logoutUser(@RequestBody @Valid TokenRefreshRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok().body("Logout successful");
    }
}