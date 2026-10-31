package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.dto.request.LoginRequest;
import com.safedriving.dto.request.RegisterRequest;
import com.safedriving.dto.response.AuthResponse;
import com.safedriving.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.safedriving.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping({"/auth", "/api/v1/auth"})
@RequiredArgsConstructor
@Tag(name = "Authentication & System", description = "APIs Xác thực người dùng, cấp phát JWT Token và kiểm tra hệ thống")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập hệ thống", description = "Xác thực người dùng bằng username và password. Trả về JWT Access Token.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin tài khoản hiện tại", description = "Lấy thông tin profile người dùng từ Bearer Token hiện tại.")
    public ResponseEntity<ApiResponse<AuthResponse>> getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        AuthResponse response = authService.getMe(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin tài khoản thành công", response));
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản Quản lý (Manager)", description = "Đăng ký tài khoản Quản lý cho người dùng trên Web. Tự động tạo bản ghi Account (vai trò MANAGER) và hồ sơ nhân sự (Staff) tương ứng.")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký tài khoản thành công", response));
    }

    @GetMapping("/health")
    @Operation(summary = "Kiểm tra sức khỏe dịch vụ Auth", description = "Kiểm tra trạng thái hoạt động của dịch vụ xác thực.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Dịch vụ xác thực hoạt động bình thường", Map.of(
                "status", "UP",
                "service", "Safe Driving Authentication Service",
                "timestamp", LocalDateTime.now()
        )));
    }

    @GetMapping("/debug-demo")
    @Operation(summary = "Endpoint thử nghiệm Debug", description = "Mô phỏng lỗi thuật toán vòng lặp để thực hành Debug với Breakpoint")
    public ResponseEntity<ApiResponse<Integer>> debugDemo() {
        int[] scores = {85, 90, 78, 92, 88}; // Danh sách 5 điểm đánh giá của tài xế
        int totalScore = 0;

        // CỐ TÌNH TẠO LỖI THUẬT TOÁN (Off-by-one): mảng có 5 phần tử (index 0..4) nhưng duyệt tới index 5
        for (int i = 0; i <= scores.length; i++) {
            totalScore += scores[i]; // <-- BẠN HÃY ĐẶT BREAKPOINT TẠI DÒNG NÀY ĐỂ SOI BIẾN!
        }

        return ResponseEntity.ok(ApiResponse.success("Tính điểm thành công", totalScore));
    }
}
