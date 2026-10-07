package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.dto.request.AssignDriverRequest;
import com.safedriving.dto.response.VehicleResponse;
import com.safedriving.entity.enums.VehicleStatus;
import com.safedriving.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/vehicles", "/api/v1/vehicles"})
@RequiredArgsConstructor
@Tag(name = "Vehicle & Fleet Management", description = "APIs Quản lý Đội xe, lọc trạng thái và phân công tài xế")
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    @Operation(summary = "Lấy danh sách phương tiện", description = "Lấy toàn bộ danh sách xe hoặc lọc theo trạng thái AVAILABLE, IN_USE, MAINTENANCE...")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getAllVehicles(
            @RequestParam(required = false) VehicleStatus status) {
        List<VehicleResponse> responses = vehicleService.getAllVehicles(status);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phương tiện thành công", responses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết một phương tiện", description = "Tra cứu thông tin chi tiết một xe theo ID.")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleById(@PathVariable String id) {
        VehicleResponse response = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin phương tiện thành công", response));
    }

    @PostMapping("/{vehicleId}/assign-driver")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Phân công tài xế cho phương tiện", description = "Điều phối viên hoặc Quản lý gán phương tiện cho tài xế phục vụ ca lái.")
    public ResponseEntity<ApiResponse<VehicleResponse>> assignDriver(
            @PathVariable String vehicleId,
            @Valid @RequestBody AssignDriverRequest request) {
        VehicleResponse response = vehicleService.assignDriver(vehicleId, request);
        return ResponseEntity.ok(ApiResponse.success("Phân công phương tiện cho tài xế thành công", response));
    }
}
