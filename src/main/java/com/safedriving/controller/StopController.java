package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.dto.request.StopRequest;
import com.safedriving.dto.response.StopResponse;
import com.safedriving.service.StopService;
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
@RequestMapping("/stops")
@RequiredArgsConstructor
@Tag(name = "Stops", description = "APIs Quản lý danh mục điểm dừng, bến xe, trạm đỗ")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class StopController {

    private final StopService stopService;

    @GetMapping
    @Operation(summary = "Xem danh sách tất cả điểm dừng", description = "Lấy toàn bộ danh sách điểm dừng trong hệ thống.")
    public ResponseEntity<ApiResponse<List<StopResponse>>> getAllStops() {
        List<StopResponse> responses = stopService.getAllStops();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách điểm dừng thành công", responses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Tra cứu chi tiết một điểm dừng", description = "Xem thông tin chi tiết một điểm dừng theo ID.")
    public ResponseEntity<ApiResponse<StopResponse>> getStopById(@PathVariable String id) {
        StopResponse response = stopService.getStopById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin điểm dừng thành công", response));
    }

    @PostMapping
    @Operation(summary = "Thêm mới điểm dừng", description = "Tạo mới điểm dừng, trạm đón trả khách kèm địa chỉ liên kết.")
    public ResponseEntity<ApiResponse<StopResponse>> createStop(@Valid @RequestBody StopRequest request) {
        StopResponse response = stopService.createStop(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm mới điểm dừng thành công", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin điểm dừng", description = "Cập nhật dữ liệu điểm dừng đã có trong hệ thống.")
    public ResponseEntity<ApiResponse<StopResponse>> updateStop(
            @PathVariable String id,
            @Valid @RequestBody StopRequest request) {
        StopResponse response = stopService.updateStop(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật điểm dừng thành công", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa một điểm dừng", description = "Xóa điểm dừng theo ID.")
    public ResponseEntity<ApiResponse<Void>> deleteStop(@PathVariable String id) {
        stopService.deleteStop(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa điểm dừng thành công"));
    }
}
