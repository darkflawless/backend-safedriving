package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.dto.request.DeviceRequest;
import com.safedriving.dto.response.DeviceResponse;
import com.safedriving.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/devices")
@RequiredArgsConstructor
@Tag(name = "Devices", description = "Quản lý thiết bị IoT giám sát hành trình")
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping
    @Operation(summary = "Lấy danh sách thiết bị", description = "Truy xuất danh sách tất cả các thiết bị IoT đang được quản lý")
    public ResponseEntity<ApiResponse<List<DeviceResponse>>> getAllDevices() {
        List<DeviceResponse> devices = deviceService.getAllDevices();
        return ResponseEntity.ok(ApiResponse.<List<DeviceResponse>>builder()
                .success(true)
                .data(devices)
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Tra cứu thiết bị", description = "Lấy thông tin chi tiết của một thiết bị theo ID")
    public ResponseEntity<ApiResponse<DeviceResponse>> getDeviceById(@PathVariable String id) {
        DeviceResponse device = deviceService.getDeviceById(id);
        return ResponseEntity.ok(ApiResponse.<DeviceResponse>builder()
                .success(true)
                .data(device)
                .build());
    }

    @PostMapping
    @Operation(summary = "Đăng ký thiết bị", description = "Đăng ký thiết bị IoT mới vào hệ thống")
    public ResponseEntity<ApiResponse<DeviceResponse>> createDevice(
            @Valid @RequestBody DeviceRequest request) {
        DeviceResponse createdDevice = deviceService.createDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<DeviceResponse>builder()
                        .success(true)
                        .data(createdDevice)
                        .build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thiết bị", description = "Cập nhật thông tin (trạng thái, MAC, Serial) của thiết bị")
    public ResponseEntity<ApiResponse<DeviceResponse>> updateDevice(
            @PathVariable String id,
            @RequestBody DeviceRequest request) {
        DeviceResponse updatedDevice = deviceService.updateDevice(id, request);
        return ResponseEntity.ok(ApiResponse.<DeviceResponse>builder()
                .success(true)
                .data(updatedDevice)
                .build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa thiết bị", description = "Xóa mềm thiết bị khỏi hệ thống")
    public ResponseEntity<ApiResponse<String>> deleteDevice(@PathVariable String id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Xóa thiết bị thành công")
                .build());
    }
}
