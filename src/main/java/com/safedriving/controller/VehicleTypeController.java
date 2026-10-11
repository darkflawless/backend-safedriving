package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.dto.request.VehicleTypeRequest;
import com.safedriving.dto.response.VehicleTypeResponse;
import com.safedriving.service.VehicleTypeService;
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

/**
 * Controller quản lý loại phương tiện (Vehicle Type).
 *
 * @author phanthuy0811
 */
@RestController
@RequestMapping({"/vehicle-types", "/api/v1/vehicle-types"})
@RequiredArgsConstructor
@Tag(name = "Vehicle Type Management", description = "APIs Quản lý danh mục loại xe (chỗ ngồi, phân loại)")
public class VehicleTypeController {

    private final VehicleTypeService vehicleTypeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    @Operation(summary = "Xem danh sách loại xe", description = "Lấy toàn bộ danh mục loại xe trong hệ thống.")
    public ResponseEntity<ApiResponse<List<VehicleTypeResponse>>> getAll() {
        List<VehicleTypeResponse> list = vehicleTypeService.getAll();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách loại xe thành công", list));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    @Operation(summary = "Xem chi tiết một loại xe theo ID")
    public ResponseEntity<ApiResponse<VehicleTypeResponse>> getById(@PathVariable Short id) {
        VehicleTypeResponse response = vehicleTypeService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin loại xe thành công", response));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Thêm mới loại xe", description = "Tạo một loại xe mới (ADMIN, MANAGER)")
    public ResponseEntity<ApiResponse<VehicleTypeResponse>> create(@Valid @RequestBody VehicleTypeRequest request) {
        VehicleTypeResponse response = vehicleTypeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm mới loại xe thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Cập nhật loại xe", description = "Chỉnh sửa mã, tên hoặc sức chứa của loại xe")
    public ResponseEntity<ApiResponse<VehicleTypeResponse>> update(
            @PathVariable Short id,
            @Valid @RequestBody VehicleTypeRequest request) {
        VehicleTypeResponse response = vehicleTypeService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật loại xe thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Xóa loại xe")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Short id) {
        vehicleTypeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa loại xe thành công"));
    }
}
