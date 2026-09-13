package com.safedriving.backend.controller;

import com.safedriving.backend.dto.common.ApiResponse;
import com.safedriving.backend.dto.request.VehicleRequest;
import com.safedriving.backend.dto.response.VehicleResponse;
import com.safedriving.backend.service.VehicleService;
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
@RequestMapping("/vehicles")
@RequiredArgsConstructor
@Tag(name = "03. Vehicles", description = "Quản lý thông tin xe, ODO, tình trạng hoạt động")

public class VehicleController {
    private final VehicleService vehicleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    @Operation(summary = "Xem danh sách xe (ADMIN, MANAGER, DRIVER)")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách xe thành công", vehicleService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    @Operation(summary = "Xem thông tin xe theo ID (ADMIN, MANAGER, DRIVER)")
    public ResponseEntity<ApiResponse<VehicleResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin xe thành công", vehicleService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Thêm mới xe vào đội (ADMIN, MANAGER)")
    public ResponseEntity<ApiResponse<VehicleResponse>> create(@Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm mới xe thành công", vehicleService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Cập nhật thông tin xe, ODO, trạng thái (ADMIN, MANAGER)")
    public ResponseEntity<ApiResponse<VehicleResponse>> update(@PathVariable String id,
                                                               @Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin xe thành công", vehicleService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Xóa xe - Soft delete (ADMIN, MANAGER)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        vehicleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa xe thành công"));
    }
}
