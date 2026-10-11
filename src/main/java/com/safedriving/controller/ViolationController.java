package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.dto.request.ViolationRequest;
import com.safedriving.dto.response.ViolationResponse;
import com.safedriving.service.ViolationService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/violations")
@RequiredArgsConstructor
@Tag(name = "Violations", description = "APIs Quản lý biên bản và chế tài vi phạm lái xe")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ViolationController {

    private final ViolationService violationService;

    @GetMapping
    @Operation(summary = "Xem danh sách biên bản vi phạm", description = "Lấy danh sách tất cả biên bản vi phạm, có thể lọc theo driverId.")
    public ResponseEntity<ApiResponse<List<ViolationResponse>>> getAllViolations(
            @RequestParam(required = false) String driverId) {
        List<ViolationResponse> responses = violationService.getAllViolations(driverId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách biên bản vi phạm thành công", responses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Tra cứu chi tiết một biên bản vi phạm", description = "Xem chi tiết một biên bản vi phạm theo ID.")
    public ResponseEntity<ApiResponse<ViolationResponse>> getViolationById(@PathVariable String id) {
        ViolationResponse response = violationService.getViolationById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin biên bản vi phạm thành công", response));
    }

    @PostMapping
    @Operation(summary = "Lập biên bản vi phạm mới", description = "Tạo mới một biên bản xử lý vi phạm giao thông / an toàn lái xe.")
    public ResponseEntity<ApiResponse<ViolationResponse>> createViolation(
            @Valid @RequestBody ViolationRequest request) {
        ViolationResponse response = violationService.createViolation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lập biên bản vi phạm thành công", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin biên bản vi phạm", description = "Cập nhật dữ liệu biên bản vi phạm đã lập theo ID.")
    public ResponseEntity<ApiResponse<ViolationResponse>> updateViolation(
            @PathVariable String id,
            @Valid @RequestBody ViolationRequest request) {
        ViolationResponse response = violationService.updateViolation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật biên bản vi phạm thành công", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa một biên bản vi phạm", description = "Xóa biên bản vi phạm theo ID.")
    public ResponseEntity<ApiResponse<Void>> deleteViolation(@PathVariable String id) {
        violationService.deleteViolation(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa biên bản vi phạm thành công"));
    }
}
