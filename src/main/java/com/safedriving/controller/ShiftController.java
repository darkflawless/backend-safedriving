package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.dto.request.ShiftEndRequest;
import com.safedriving.dto.request.ShiftStartRequest;
import com.safedriving.dto.response.ShiftResponse;
import com.safedriving.security.CustomUserDetails;
import com.safedriving.service.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/shifts", "/api/v1/shifts"})
@RequiredArgsConstructor
@Tag(name = "Shift Lifecycle", description = "APIs Quản lý vòng đời ca lái (Check-in, Check-out, Cấp phát MQTT)")
public class ShiftController {

    private final ShiftService shiftService;

    @PostMapping("/start")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Bắt đầu ca lái (Check-in ca lái tự động)",
            description = "Tự động nhận diện xe đã phân công cho tài xế, chuyển xe sang IN_USE, sinh cấu hình MQTT và Client ID ngẫu nhiên.")
    public ResponseEntity<ApiResponse<ShiftResponse>> startShift(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody(required = false) ShiftStartRequest request) {
        ShiftResponse response = shiftService.startShift(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bắt đầu ca lái thành công", response));
    }

    @PostMapping("/end")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Kết thúc ca lái (Check-out ca lái)",
            description = "Chốt số odometer và phần trăm pin cuối ca, giải phóng xe về trạng thái AVAILABLE.")
    public ResponseEntity<ApiResponse<ShiftResponse>> endShift(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ShiftEndRequest request) {
        ShiftResponse response = shiftService.endShift(userDetails, request);
        return ResponseEntity.ok(ApiResponse.success("Kết thúc ca lái thành công", response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Lấy ca lái đang chạy",
            description = "Tra cứu ca lái trạng thái ACTIVE hiện tại của tài xế đang đăng nhập.")
    public ResponseEntity<ApiResponse<ShiftResponse>> getActiveShift(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ShiftResponse response = shiftService.getActiveShift(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Lấy ca lái đang hoạt động thành công", response));
    }
}
