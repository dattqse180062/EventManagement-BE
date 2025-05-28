package swd392.eventmanagement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import swd392.eventmanagement.model.dto.request.DepartmentRequest;
import swd392.eventmanagement.service.impl.DepartmentServiceImpl;

import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentServiceImpl departmentService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createDepartment(@RequestBody DepartmentRequest requestDTO) {
        departmentService.createDepartment(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Tạo phòng ban thành công"));
    }
}
