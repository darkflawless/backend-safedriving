package com.safedriving.backend.controller;

import com.safedriving.backend.dto.common.ApiResponse;
import com.safedriving.backend.dto.request.IncidentRequest;
import com.safedriving.backend.dto.response.IncidentResponse;
import com.safedriving.backend.service.IncidentService;
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
@RequestMapping("/incidents")
@RequiredArgsConstructor
@Tag(name = "06. Incidents", description = "Ghi nhận sự cố kỹ thuật, hỏng hóc, tai nạn")
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    @Operation(summary = "Xem danh sách tất cả sự cố (ADMIN, MANAGER, DRIVER)")
    public ResponseEntity<ApiResponse<List<IncidentResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sự cố thành công", incidentService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    @Operation(summary = "Xem chi tiết sự cố theo ID (ADMIN, MANAGER, DRIVER)")
    public ResponseEntity<ApiResponse<IncidentResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết sự cố thành công", incidentService.getById(id)));
    }

    @GetMapping("/trip/{tripId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    @Operation(summary = "Xem sự cố theo chuyến đi (ADMIN, MANAGER, DRIVER)")
    public ResponseEntity<ApiResponse<List<IncidentResponse>>> getByTripId(@PathVariable String tripId) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sự cố của chuyến đi thành công", incidentService.getByTripId(tripId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    @Operation(summary = "Báo cáo sự cố trên đường (ADMIN, MANAGER, DRIVER)")
    public ResponseEntity<ApiResponse<IncidentResponse>> create(@Valid @RequestBody IncidentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Báo cáo sự cố thành công", incidentService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    @Operation(summary = "Cập nhật thông tin sự cố (ADMIN, MANAGER, DRIVER)")
    public ResponseEntity<ApiResponse<IncidentResponse>> update(@PathVariable String id,
                                                                @Valid @RequestBody IncidentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật sự cố thành công", incidentService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Xóa sự cố (Chỉ ADMIN, MANAGER)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        incidentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa bản ghi sự cố"));
    }
}
