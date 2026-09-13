package com.safedriving.backend.controller;

import com.safedriving.backend.dto.common.ApiResponse;
import com.safedriving.backend.dto.request.VehicleTypeRequest;
import com.safedriving.backend.dto.response.VehicleTypeResponse;
import com.safedriving.backend.service.VehicleTypeService;
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
@RequestMapping("/vehicle-types")
@RequiredArgsConstructor
@Tag(name = "02. Vehicle Types", description = "Quản lý loại phương tiện (16 chỗ, 29 chỗ, 45 chỗ...)")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class VehicleTypeController {

    private final VehicleTypeService vehicleTypeService;

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả loại xe")
    public ResponseEntity<ApiResponse<List<VehicleTypeResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách loại xe thành công", vehicleTypeService.getAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết loại xe theo ID")
    public ResponseEntity<ApiResponse<VehicleTypeResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết loại xe thành công", vehicleTypeService.getById(id)));
    }

    @PostMapping
    @Operation(summary = "Thêm loại xe mới")
    public ResponseEntity<ApiResponse<VehicleTypeResponse>> create(@Valid @RequestBody VehicleTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo loại xe mới thành công", vehicleTypeService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật loại xe")
    public ResponseEntity<ApiResponse<VehicleTypeResponse>> update(@PathVariable Integer id,
                                                                   @Valid @RequestBody VehicleTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật loại xe thành công", vehicleTypeService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa loại xe")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        vehicleTypeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa loại xe thành công"));
    }
}