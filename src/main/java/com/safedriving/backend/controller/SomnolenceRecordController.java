package com.safedriving.backend.controller;

import com.safedriving.backend.dto.common.ApiResponse;
import com.safedriving.backend.dto.request.SomnolenceRecordRequest;
import com.safedriving.backend.dto.response.SomnolenceRecordResponse;
import com.safedriving.backend.service.SomnolenceRecordService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/somnolence-records")
@RequiredArgsConstructor
@Tag(name = "07. Somnolence Records", description = "Tiếp nhận & cảnh báo ngủ gật / mất tập trung")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class SomnolenceRecordController {

    private final SomnolenceRecordService service;

    @GetMapping
    @Operation(summary = "Lấy toàn bộ danh sách cảnh báo ngủ gật (ADMIN, MANAGER)")
    public ResponseEntity<ApiResponse<List<SomnolenceRecordResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách cảnh báo thành công", service.getAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết một cảnh báo theo ID (ADMIN, MANAGER)")
    public ResponseEntity<ApiResponse<SomnolenceRecordResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết cảnh báo thành công", service.getById(id)));
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Lấy danh sách cảnh báo ngủ gật của 1 tài xế (ADMIN, MANAGER)")
    public ResponseEntity<ApiResponse<List<SomnolenceRecordResponse>>> getByDriverId(@PathVariable String driverId) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách theo tài xế thành công", service.getByDriverId(driverId)));
    }

    @PostMapping
    @Operation(summary = "Tiếp nhận cảnh báo từ AI Camera/Thiết bị IoT (ADMIN, MANAGER)")
    public ResponseEntity<ApiResponse<SomnolenceRecordResponse>> create(@Valid @RequestBody SomnolenceRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tiếp nhận cảnh báo ngủ gật thành công", service.create(request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa bản ghi cảnh báo (ADMIN, MANAGER)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa bản ghi cảnh báo thành công"));
    }
}
