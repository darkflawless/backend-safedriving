package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.dto.request.StaffRequest;
import com.safedriving.dto.response.StaffResponse;
import com.safedriving.service.StaffService;
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
@RequestMapping("/staff")
@RequiredArgsConstructor
@Tag(name = "Staff", description = "APIs Quản lý hồ sơ Cán bộ, Nhân viên")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    @Operation(summary = "Xem toàn bộ danh sách cán bộ, nhân viên", description = "Lấy toàn bộ danh sách hồ sơ nhân sự trong hệ thống.")
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getAllStaff() {
        List<StaffResponse> responses = staffService.getAllStaff();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nhân viên thành công", responses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết hồ sơ nhân sự theo ID", description = "Tra cứu thông tin chi tiết của một nhân viên theo mã định danh (ID).")
    public ResponseEntity<ApiResponse<StaffResponse>> getStaffById(@PathVariable String id) {
        StaffResponse response = staffService.getStaffById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin nhân viên thành công", response));
    }

    @PostMapping
    @Operation(summary = "Tạo mới hồ sơ nhân sự", description = "Tạo mới hồ sơ nhân sự (Họ tên, ngày sinh, giới tính, email, số điện thoại, địa chỉ và liên kết tài khoản account).")
    public ResponseEntity<ApiResponse<StaffResponse>> createStaff(@Valid @RequestBody StaffRequest request) {
        StaffResponse response = staffService.createStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo mới hồ sơ nhân sự thành công", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin nhân viên", description = "Cập nhật thông tin hồ sơ nhân sự theo ID.")
    public ResponseEntity<ApiResponse<StaffResponse>> updateStaff(
            @PathVariable String id,
            @Valid @RequestBody StaffRequest request) {
        StaffResponse response = staffService.updateStaff(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin nhân viên thành công", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa hồ sơ nhân sự", description = "Xóa hồ sơ nhân sự theo ID.")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(@PathVariable String id) {
        staffService.deleteStaff(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa hồ sơ nhân sự thành công"));
    }
}
