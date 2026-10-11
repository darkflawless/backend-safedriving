package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.dto.request.VehicleLogRequest;
import com.safedriving.dto.response.VehicleLogResponse;
import com.safedriving.service.VehicleLogService;
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
@RequestMapping("/vehicle-logs")
@RequiredArgsConstructor
@Tag(name = "Vehicle Logs", description = "APIs Nhật ký tọa độ vị trí GPS lịch sử của xe")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class VehicleLogController {

    private final VehicleLogService vehicleLogService;

    @GetMapping
    @Operation(summary = "Xem danh sách nhật ký vị trí xe", description = "Lấy toàn bộ nhật ký vị trí xe, có thể lọc theo vehicleId.")
    public ResponseEntity<ApiResponse<List<VehicleLogResponse>>> getAllVehicleLogs(
            @RequestParam(required = false) String vehicleId) {
        List<VehicleLogResponse> responses = vehicleLogService.getAllVehicleLogs(vehicleId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nhật ký phương tiện thành công", responses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Tra cứu chi tiết một bản ghi nhật ký vị trí", description = "Xem thông tin chi tiết một bản ghi nhật ký vị trí theo ID.")
    public ResponseEntity<ApiResponse<VehicleLogResponse>> getVehicleLogById(@PathVariable Long id) {
        VehicleLogResponse response = vehicleLogService.getVehicleLogById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin nhật ký phương tiện thành công", response));
    }

    @PostMapping
    @Operation(summary = "Thêm mới bản ghi nhật ký vị trí xe", description = "Ghi nhận tọa độ GPS và thời gian di chuyển của xe.")
    public ResponseEntity<ApiResponse<VehicleLogResponse>> createVehicleLog(
            @Valid @RequestBody VehicleLogRequest request) {
        VehicleLogResponse response = vehicleLogService.createVehicleLog(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm mới nhật ký phương tiện thành công", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật bản ghi nhật ký vị trí xe", description = "Cập nhật thông tin bản ghi nhật ký vị trí theo ID.")
    public ResponseEntity<ApiResponse<VehicleLogResponse>> updateVehicleLog(
            @PathVariable Long id,
            @Valid @RequestBody VehicleLogRequest request) {
        VehicleLogResponse response = vehicleLogService.updateVehicleLog(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật nhật ký phương tiện thành công", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa một bản ghi nhật ký vị trí xe", description = "Xóa bản ghi nhật ký vị trí xe theo ID.")
    public ResponseEntity<ApiResponse<Void>> deleteVehicleLog(@PathVariable Long id) {
        vehicleLogService.deleteVehicleLog(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa nhật ký phương tiện thành công"));
    }
}
