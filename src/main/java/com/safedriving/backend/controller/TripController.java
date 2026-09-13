package com.safedriving.backend.controller;

import com.safedriving.backend.dto.common.ApiResponse;
import com.safedriving.backend.dto.request.TripRequest;
import com.safedriving.backend.dto.response.TripResponse;
import com.safedriving.backend.service.TripService;
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
@RequestMapping("/trips")
@RequiredArgsConstructor
@Tag(name = "04. Trips", description = "Lập kế hoạch và quản lý chuyến đi")
public class TripController {

    private final TripService tripService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    @Operation(summary = "Xem danh sách chuyến đi (ADMIN, MANAGER, DRIVER)")
    public ResponseEntity<ApiResponse<List<TripResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chuyến đi thành công", tripService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    @Operation(summary = "Xem chi tiết chuyến đi (ADMIN, MANAGER, DRIVER)")
    public ResponseEntity<ApiResponse<TripResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chuyến đi thành công", tripService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Lập kế hoạch chuyến đi mới (ADMIN, MANAGER)")
    public ResponseEntity<ApiResponse<TripResponse>> create(@Valid @RequestBody TripRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lập kế hoạch chuyến đi thành công", tripService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Cập nhật chuyến đi (ADMIN, MANAGER)")
    public ResponseEntity<ApiResponse<TripResponse>> update(@PathVariable String id,
                                                            @Valid @RequestBody TripRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật chuyến đi thành công", tripService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Hủy/Xóa chuyến đi - Soft delete (ADMIN, MANAGER)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        tripService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa chuyến đi thành công"));
    }
}
