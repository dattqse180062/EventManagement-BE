package swd392.eventmanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Health check API")
public class HealthController {

    @GetMapping("/check")
    @Operation(summary = "Health check", description = "Check if the API is running")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("API is running");
    }

    @GetMapping("/cicd")
    @Operation(summary = "CI/CD test", description = "Endpoint to test CI/CD pipeline")
    public ResponseEntity<String> cicdTest() {
        return ResponseEntity.ok("CI/CD pipeline is working correctly! Current version: 1.0");
    }
}
