package com.safedriving.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.safedriving.entity.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo hồ sơ tài xế trọn gói (Account + Staff + Driver)")
public class DriverCreateRequest {

    // 1. Tài khoản đăng nhập (Account)
    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username phải từ 3 đến 50 ký tự")
    @Schema(description = "Tên đăng nhập cho tài xế", example = "driver_vana")
    private String username;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Password phải có ít nhất 6 ký tự")
    @Schema(description = "Mật khẩu tài khoản", example = "Password123@")
    private String password;

    // 2. Thông tin cá nhân & nhân sự (Staff)
    @NotBlank(message = "Họ không được để trống")
    @Schema(description = "Họ và tên đệm", example = "Văn A")
    private String firstName;

    @NotBlank(message = "Tên không được để trống")
    @Schema(description = "Tên chính", example = "Nguyễn")
    private String lastName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày sinh (yyyy-MM-dd)", example = "1990-01-15")
    private LocalDate dateOfBirth;

    @Schema(description = "Giới tính (MALE, FEMALE, OTHER)", example = "MALE")
    private Gender gender;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Schema(description = "Số điện thoại liên hệ", example = "0901234567")
    private String phone;

    @Email(message = "Email không đúng định dạng")
    @Schema(description = "Địa chỉ email", example = "vana@example.com")
    private String email;

    @Schema(description = "Địa chỉ cụ thể (số nhà, ngõ, đường)", example = "123 Phố Huế")
    private String exactAddress;

    @Schema(description = "Phường/Xã", example = "Hàng Bài")
    private String commune;

    @Schema(description = "Tỉnh/Thành phố", example = "Hà Nội")
    private String province;

    // 3. Nghiệp vụ tài xế (Driver)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày tuyển dụng (mặc định hôm nay nếu null)", example = "2024-05-01")
    private LocalDate hireDate;

    @Schema(description = "Đường dẫn ảnh đại diện/chân dung tài xế (nếu đã có URL sẵn)", example = "https://example.com/driver-photo.jpg")
    private String urlImage;

    @Builder.Default
    @Schema(description = "Trạng thái hoạt động của tài xế", example = "true")
    private Boolean isActive = true;

    @Schema(description = "ID xe được giao dạng phẳng (tùy chọn)", example = "veh-uuid-1")
    private String vehicleId;
}
