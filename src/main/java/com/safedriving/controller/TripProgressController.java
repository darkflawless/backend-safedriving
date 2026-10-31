package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.dto.request.TripProgressRequest;
import com.safedriving.dto.response.TripProgressResponse;
import com.safedriving.service.TripProgressService;
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
@RequestMapping("/trip-progress")
@RequiredArgsConstructor
@Tag(name = "Trip Progress", description = "APIs Cập nhật tiến độ thực tế (giờ đến/rời từng trạm)")
public class TripProgressController {

    private final TripProgressService tripProgressService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    @Operation(summary = "Xem danh sách tiến độ chuyến đi", description = "Lấy toàn bộ nhật ký tiến độ thực tế, có thể lọc theo tripId.")
    public ResponseEntity<ApiResponse<List<TripProgressResponse>>> getAllTripProgress(
            @RequestParam(required = false) String tripId) {
        List<TripProgressResponse> responses = tripProgressService.getAllTripProgress(tripId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tiến độ chuyến đi thành công", responses));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    @Operation(summary = "Tra cứu chi tiết một bản ghi tiến độ", description = "Xem chi tiết một bản ghi tiến độ chuyến đi theo ID.")
    public ResponseEntity<ApiResponse<TripProgressResponse>> getTripProgressById(@PathVariable String id) {
        TripProgressResponse response = tripProgressService.getTripProgressById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin tiến độ chuyến đi thành công", response));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    @Operation(summary = "Ghi nhận tiến độ chuyến đi", description = "Ghi nhận thời điểm đến và khởi hành tại trạm của chuyến xe.")
    public ResponseEntity<ApiResponse<TripProgressResponse>> createTripProgress(
            @Valid @RequestBody TripProgressRequest request) {
        TripProgressResponse response = tripProgressService.createTripProgress(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ghi nhận tiến độ chuyến đi thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    @Operation(summary = "Cập nhật thông tin tiến độ chuyến đi", description = "Cập nhật thời gian đến/rời trạm thực tế theo ID.")
    public ResponseEntity<ApiResponse<TripProgressResponse>> updateTripProgress(
            @PathVariable String id,
            @Valid @RequestBody TripProgressRequest request) {
        TripProgressResponse response = tripProgressService.updateTripProgress(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tiến độ chuyến đi thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Xóa bản ghi tiến độ chuyến đi", description = "Xóa bản ghi tiến độ chuyến đi theo ID.")
    public ResponseEntity<ApiResponse<Void>> deleteTripProgress(@PathVariable String id) {
        tripProgressService.deleteTripProgress(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa tiến độ chuyến đi thành công"));
    }
}
