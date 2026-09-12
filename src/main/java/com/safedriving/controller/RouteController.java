package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.dto.request.RouteRequest;
import com.safedriving.dto.response.RouteResponse;
import com.safedriving.service.RouteService;
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
@RequestMapping("/routes")
@RequiredArgsConstructor
@Tag(name = "Routes", description = "APIs Quản lý danh mục tuyến đường vận chuyển")
public class RouteController {

    private final RouteService routeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    @Operation(summary = "Xem danh sách tất cả các tuyến đường", description = "Lấy toàn bộ danh sách tuyến đường vận chuyển.")
    public ResponseEntity<ApiResponse<List<RouteResponse>>> getAllRoutes() {
        List<RouteResponse> responses = routeService.getAllRoutes();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tuyến đường thành công", responses));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    @Operation(summary = "Tra cứu chi tiết một tuyến đường", description = "Xem thông tin chi tiết một tuyến đường theo ID.")
    public ResponseEntity<ApiResponse<RouteResponse>> getRouteById(@PathVariable String id) {
        RouteResponse response = routeService.getRouteById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin tuyến đường thành công", response));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Thêm mới tuyến đường", description = "Tạo mới một tuyến đường vận chuyển với cự ly và thời gian chạy tiêu chuẩn.")
    public ResponseEntity<ApiResponse<RouteResponse>> createRoute(@Valid @RequestBody RouteRequest request) {
        RouteResponse response = routeService.createRoute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm mới tuyến đường thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Cập nhật thông tin tuyến đường", description = "Cập nhật dữ liệu tuyến đường đã có trong hệ thống.")
    public ResponseEntity<ApiResponse<RouteResponse>> updateRoute(
            @PathVariable String id,
            @Valid @RequestBody RouteRequest request) {
        RouteResponse response = routeService.updateRoute(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tuyến đường thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Xóa một tuyến đường", description = "Xóa bản ghi tuyến đường theo ID.")
    public ResponseEntity<ApiResponse<Void>> deleteRoute(@PathVariable String id) {
        routeService.deleteRoute(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa tuyến đường thành công"));
    }
}
