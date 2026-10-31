package com.safedriving.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.safedriving.entity.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Thông tin yêu cầu đăng ký tài khoản Quản lý (Manager)")
public class RegisterRequest {

    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username phải từ 3 đến 50 ký tự")
    @Schema(description = "Tên đăng nhập", example = "manager_nguyenvana")
    private String username;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Password phải có ít nhất 6 ký tự")
    @Schema(description = "Mật khẩu", example = "Password123@")
    private String password;

    @NotBlank(message = "Họ không được để trống")
    @Schema(description = "Họ và tên đệm", example = "Van A")
    private String firstName;

    @NotBlank(message = "Tên không được để trống")
    @Schema(description = "Tên chính", example = "Nguyen")
    private String lastName;

    @NotNull(message = "Ngày sinh không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày sinh (yyyy-MM-dd)", example = "1995-05-20")
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
}
