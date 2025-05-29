package swd392.eventmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swd392.eventmanagement.model.dto.request.TagRequest;
import swd392.eventmanagement.service.impl.TagServiceImpl;

import java.util.Map;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor

public class TagController {

    private final TagServiceImpl tagService;

    @PostMapping("/create")
    @Operation(
            summary = "Create new tag",
            description = "Tạo một thẻ mới trong hệ thống",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(
            responseCode = "201",
            description = "Tạo tag thành công",
            content = @Content(schema = @Schema(implementation = Map.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Dữ liệu không hợp lệ hoặc tag đã tồn tại"
    )
    public ResponseEntity<Map<String, String>> createTag(@RequestBody TagRequest request) {
        tagService.createTag(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Tạo tag thành công"));
    }
    @PutMapping("/update/{id}")
    @Operation(
            summary = "Update tag",
            description = "Cập nhật thông tin tag theo ID",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200",description = "Cập nhật tag thành công")
    @ApiResponse(responseCode = "404",description = "Không tìm thấy tag")
    public ResponseEntity<Map<String, String>> updateTag(@PathVariable Long id,@RequestBody TagRequest request) {
        tagService.updateTag(id, request);
        return ResponseEntity.ok(Map.of("message","Cập nhật tag thành công"));
    }

}
