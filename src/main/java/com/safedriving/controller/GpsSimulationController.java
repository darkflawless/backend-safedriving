package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.service.GpsSimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping({ "/simulation", "/api/v1/simulation" })
@RequiredArgsConstructor
@Tag(name = "GPS Simulation E2E", description = "APIs Mô phỏng lộ trình xe chạy tự động 9.4 km để kiểm thử Geofence điểm đen tai nạn")
public class GpsSimulationController {

    private final GpsSimulationService gpsSimulationService;

    @GetMapping("/route")
    @Operation(summary = "Lấy dữ liệu lộ trình 9.4 km (393 điểm OSRM chuẩn tim đường Hà Nội)",
            description = "Cung cấp tọa độ chính xác để Web Leaflet và Mobile App vẽ lộ trình và ghim điểm đen")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSimulationRoute() {
        Map<String, Object> data = gpsSimulationService.getRouteData();
        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu lộ trình thành công", data));
    }

    @PostMapping("/start")
    @Operation(summary = "Khởi chạy mô phỏng GPS xe di chuyển",
            description = "Xe bắt đầu chạy từ Bến xe Mỹ Đình (hoặc vị trí chỉ định), phát GPS liên tục mỗi 1-2s và kích hoạt Geofence khi vào vùng 200m")
    public ResponseEntity<ApiResponse<Map<String, Object>>> startSimulation(
            @RequestParam(required = false, defaultValue = "trip-e2e-001") String tripId,
            @RequestParam(required = false, defaultValue = "2.0") Double speedMultiplier,
            @RequestParam(required = false) Integer startIndex) {
        Map<String, Object> result = gpsSimulationService.startSimulation(tripId, speedMultiplier, startIndex);
        return ResponseEntity.ok(ApiResponse.success("Bắt đầu mô phỏng GPS", result));
    }

    @PostMapping("/stop")
    @Operation(summary = "Dừng mô phỏng GPS xe di chuyển", description = "Tạm dừng hoặc kết thúc việc mô phỏng xe chạy")
    public ResponseEntity<ApiResponse<Map<String, Object>>> stopSimulation() {
        Map<String, Object> result = gpsSimulationService.stopSimulation();
        return ResponseEntity.ok(ApiResponse.success("Đã dừng mô phỏng GPS", result));
    }

    @GetMapping("/status")
    @Operation(summary = "Kiểm tra trạng thái hiện tại của mô phỏng", description = "Xem xe đang ở tọa độ nào, bước thứ mấy trên tuyến")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSimulationStatus() {
        Map<String, Object> status = gpsSimulationService.getStatus();
        return ResponseEntity.ok(ApiResponse.success("Lấy trạng thái mô phỏng", status));
    }
}
