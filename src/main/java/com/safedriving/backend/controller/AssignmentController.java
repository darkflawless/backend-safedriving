package com.safedriving.backend.controller;

import com.safedriving.backend.dto.common.ApiResponse;
import com.safedriving.backend.dto.request.AssignmentRequest;
import com.safedriving.backend.dto.response.AssignmentResponse;
import com.safedriving.backend.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/assignments")
@RequiredArgsConstructor
@Tag(name = "05. Assignments", description = "Phân công tài xế đảm nhận chuyến đi")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    @Operation(summary = "Xem tất cả phân công (ADMIN, MANAGER, DRIVER)")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phân công thành công", assignmentService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    @Operation(summary = "Xem chi tiết phân công theo ID (ADMIN, MANAGER, DRIVER)")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết phân công thành công", assignmentService.getById(id)));
    }

    @GetMapping("/driver/{driverId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    @Operation(summary = "Xem chuyến đi được phân công cho tài xế (ADMIN, MANAGER, DRIVER)")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getByDriverId(@PathVariable String driverId) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phân công của tài xế thành công", assignmentService.getByDriverId(driverId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Phân công tài xế nhận chuyến (ADMIN, MANAGER)")
    public ResponseEntity<ApiResponse<AssignmentResponse>> create(@Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Phân công tài xế thành công", assignmentService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Cập nhật phân công (ADMIN, MANAGER)")
    public ResponseEntity<ApiResponse<AssignmentResponse>> update(@PathVariable String id,
                                                                  @Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật phân công thành công", assignmentService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Hủy phân công - Soft delete (ADMIN, MANAGER)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        assignmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa phân công thành công"));
    }
}
