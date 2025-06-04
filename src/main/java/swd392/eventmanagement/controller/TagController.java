package swd392.eventmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import swd392.eventmanagement.model.dto.request.TagRequest;
import swd392.eventmanagement.model.dto.response.TagShowDTO;
import swd392.eventmanagement.service.impl.TagServiceImpl;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Tag(name = "Tag", description = "Tag API")
public class TagController {

        private final TagServiceImpl tagService;

        @PostMapping("")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        @Operation(
                summary = "Create new tag",
                description = "Creates a new tag in the system",
                security = @SecurityRequirement(name = "bearerAuth")
        )
        @ApiResponse(
                responseCode = "201",
                description = "Tag created successfully",
                content = @Content(schema = @Schema(implementation = TagShowDTO.class))
        )
        @ApiResponse(
                responseCode = "400",
                description = "Bad Request - Invalid tag data or tag already exists"
        )
        @ApiResponse(
                responseCode = "403",
                description = "Forbidden - User does not have permission"
        )
        @ApiResponse(
                responseCode = "404",
                description = "Not Found"
        )
        public ResponseEntity<TagShowDTO> createTag(@Valid @RequestBody TagRequest request) {
                TagShowDTO createdTag = tagService.createTag(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
        }

        @GetMapping("/active")
        @Operation(summary = "Get all active tags", description = "Returns a list of all active tags")
        @ApiResponse(responseCode = "200", description = "Active tags retrieved successfully", content = @Content(schema = @Schema(implementation = TagShowDTO.class)))
        @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission")
        @ApiResponse(responseCode = "404", description = "No active tags found in the system")
        public ResponseEntity<?> getActiveTags() {
                return ResponseEntity.ok(tagService.getActiveTags());
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        @Operation(
                summary = "Update tag",
                description = "Update an existing tag by its ID",
                security = @SecurityRequirement(name = "bearerAuth")
        )
        @ApiResponse(
                responseCode = "200",
                description = "Tag updated successfully",
                content = @Content(schema = @Schema(implementation = Map.class))
        )
        @ApiResponse(
                responseCode = "400",
                description = "Bad Request - Invalid tag data or tag name already exists"
        )
        @ApiResponse(
                responseCode = "403",
                description = "Forbidden - User does not have permission"
        )
        @ApiResponse(
                responseCode = "404",
                description = "Not Found - Tag with given ID does not exist"
        )
        public ResponseEntity<Map<String, String>> updateTag(
                @PathVariable Long id,
                @Valid @RequestBody TagRequest request) {
                tagService.updateTag(id, request);
                return ResponseEntity.ok(Map.of("message", "Tag updated successfully"));
        }

        @PutMapping("disable/{id}")
        @Operation(summary = "Disable tag (soft delete)",description = "Đặt isActive = false để vô hiệu hóa tag",security = @SecurityRequirement(name ="bearerAuth"))
        @ApiResponse(responseCode = "200",description = "Vô hiệu hóa tag thành công",content = @Content(schema = @Schema(implementation = Map.class)))
        @ApiResponse(responseCode = "404",description = "Không tìm thấy tag")
        public ResponseEntity<Map<String,String>> disableTag(@PathVariable Long id){
               tagService.deleteTag(id); // Hàm gắn isActive = false
               return ResponseEntity.ok(Map.of("message","Vô hiệu hóa thành công"));
        }



}




