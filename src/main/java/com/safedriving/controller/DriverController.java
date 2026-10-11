package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.dto.request.DriverRequest;
import com.safedriving.dto.response.DriverResponse;
import com.safedriving.service.DriverService;
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
@RequestMapping("/drivers")
@RequiredArgsConstructor
@Tag(name = "Drivers", description = "APIs Quản lý Hồ sơ Tài xế")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class DriverController {

    private final DriverService driverService;

    @GetMapping
    @Operation(summary = "Xem danh sách tài xế đang quản lý", description = "Lấy danh sách tất cả các tài xế còn hoạt động/chưa bị xóa.")
    public ResponseEntity<ApiResponse<List<DriverResponse>>> getAllDrivers() {
        List<DriverResponse> responses = driverService.getAllDrivers();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tài xế thành công", responses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Tra cứu hồ sơ chi tiết một tài xế", description = "Tra cứu chi tiết hồ sơ tài xế theo mã định danh (ID).")
    public ResponseEntity<ApiResponse<DriverResponse>> getDriverById(@PathVariable String id) {
        DriverResponse response = driverService.getDriverById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin tài xế thành công", response));
    }

    @PostMapping
    @Operation(summary = "Tạo hồ sơ tài xế mới", description = "Tạo hồ sơ tài xế mới (ngày tuyển dụng hireDate, URL ảnh, cờ isActive, liên kết staff, vehicle, licenseClass).")
    public ResponseEntity<ApiResponse<DriverResponse>> createDriver(@Valid @RequestBody DriverRequest request) {
        DriverResponse response = driverService.createDriver(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo hồ sơ tài xế thành công", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin hồ sơ tài xế", description = "Cập nhật dữ liệu hồ sơ tài xế theo ID.")
    public ResponseEntity<ApiResponse<DriverResponse>> updateDriver(
            @PathVariable String id,
            @Valid @RequestBody DriverRequest request) {
        DriverResponse response = driverService.updateDriver(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin tài xế thành công", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa thông tin tài xế", description = "Xóa mềm hồ sơ tài xế theo ID.")
    public ResponseEntity<ApiResponse<Void>> deleteDriver(@PathVariable String id) {
        driverService.deleteDriver(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa thông tin tài xế thành công"));
    }
}
