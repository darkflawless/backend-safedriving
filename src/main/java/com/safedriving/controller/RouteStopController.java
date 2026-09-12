package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.dto.request.RouteStopRequest;
import com.safedriving.dto.response.RouteStopResponse;
import com.safedriving.service.RouteStopService;
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
@RequestMapping("/route-stops")
@RequiredArgsConstructor
@Tag(name = "Route Stops", description = "APIs Quản lý thứ tự các điểm dừng đón/trả trên từng tuyến")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class RouteStopController {

    private final RouteStopService routeStopService;

    @GetMapping
    @Operation(summary = "Xem danh sách điểm dừng trên tuyến", description = "Lấy toàn bộ điểm dừng trên tuyến, có thể lọc theo routeId.")
    public ResponseEntity<ApiResponse<List<RouteStopResponse>>> getAllRouteStops(
            @RequestParam(required = false) String routeId) {
        List<RouteStopResponse> responses = routeStopService.getAllRouteStops(routeId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách điểm dừng trên tuyến thành công", responses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Tra cứu chi tiết một liên kết điểm dừng trên tuyến", description = "Xem thông tin chi tiết một liên kết điểm dừng trên tuyến theo ID.")
    public ResponseEntity<ApiResponse<RouteStopResponse>> getRouteStopById(@PathVariable String id) {
        RouteStopResponse response = routeStopService.getRouteStopById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin điểm dừng trên tuyến thành công", response));
    }

    @PostMapping
    @Operation(summary = "Thêm điểm dừng vào tuyến", description = "Thiết lập liên kết điểm dừng đón/trả và thứ tự trạm trên tuyến.")
    public ResponseEntity<ApiResponse<RouteStopResponse>> createRouteStop(@Valid @RequestBody RouteStopRequest request) {
        RouteStopResponse response = routeStopService.createRouteStop(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm điểm dừng vào tuyến thành công", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật liên kết điểm dừng trên tuyến", description = "Cập nhật tuyến, trạm hoặc thứ tự đón/trả theo ID.")
    public ResponseEntity<ApiResponse<RouteStopResponse>> updateRouteStop(
            @PathVariable String id,
            @Valid @RequestBody RouteStopRequest request) {
        RouteStopResponse response = routeStopService.updateRouteStop(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật điểm dừng trên tuyến thành công", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa điểm dừng khỏi tuyến", description = "Xóa bản ghi liên kết điểm dừng trên tuyến theo ID.")
    public ResponseEntity<ApiResponse<Void>> deleteRouteStop(@PathVariable String id) {
        routeStopService.deleteRouteStop(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa điểm dừng khỏi tuyến thành công"));
    }
}
