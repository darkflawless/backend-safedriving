package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.dto.request.LicenseRequest;
import com.safedriving.dto.response.LicenseResponse;
import com.safedriving.service.LicenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/licenses")
@RequiredArgsConstructor
@Tag(name = "Licenses", description = "Quản lý Giấy phép lái xe thực tế của tài xế")
public class LicenseController {

    private final LicenseService licenseService;

    @GetMapping
    @Operation(summary = "Lấy danh sách GPLX", description = "Truy xuất danh sách GPLX. Có thể lọc theo driverId.")
    public ResponseEntity<ApiResponse<List<LicenseResponse>>> getAllLicenses(
            @RequestParam(required = false) String driverId) {
        List<LicenseResponse> licenses = licenseService.getAllLicenses(driverId);
        return ResponseEntity.ok(ApiResponse.<List<LicenseResponse>>builder()
                .success(true)
                .data(licenses)
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Tra cứu GPLX", description = "Lấy thông tin chi tiết GPLX theo ID")
    public ResponseEntity<ApiResponse<LicenseResponse>> getLicenseById(@PathVariable String id) {
        LicenseResponse license = licenseService.getLicenseById(id);
        return ResponseEntity.ok(ApiResponse.<LicenseResponse>builder()
                .success(true)
                .data(license)
                .build());
    }

    @PostMapping
    @Operation(summary = "Thêm GPLX mới", description = "Đăng ký GPLX mới cho tài xế")
    public ResponseEntity<ApiResponse<LicenseResponse>> createLicense(
            @Valid @RequestBody LicenseRequest request) {
        LicenseResponse createdLicense = licenseService.createLicense(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<LicenseResponse>builder()
                        .success(true)
                        .data(createdLicense)
                        .build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật GPLX", description = "Cập nhật thông tin GPLX hiện có")
    public ResponseEntity<ApiResponse<LicenseResponse>> updateLicense(
            @PathVariable String id,
            @RequestBody LicenseRequest request) {
        LicenseResponse updatedLicense = licenseService.updateLicense(id, request);
        return ResponseEntity.ok(ApiResponse.<LicenseResponse>builder()
                .success(true)
                .data(updatedLicense)
                .build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa GPLX", description = "Xóa mềm GPLX khỏi hệ thống")
    public ResponseEntity<ApiResponse<String>> deleteLicense(@PathVariable String id) {
        licenseService.deleteLicense(id);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Xóa giấy phép lái xe thành công")
                .build());
    }
}
