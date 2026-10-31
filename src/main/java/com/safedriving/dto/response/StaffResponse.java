package com.safedriving.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.safedriving.entity.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin phản hồi hồ sơ nhân sự")
public class StaffResponse {

    @Schema(description = "ID hồ sơ nhân sự (UUID)")
    private String id;

    @Schema(description = "Họ và tên đệm")
    private String firstName;

    @Schema(description = "Tên chính")
    private String lastName;

    @Schema(description = "Họ và tên đầy đủ")
    private String fullName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày sinh")
    private LocalDate dateOfBirth;

    @Schema(description = "Giới tính")
    private Gender gender;

    @Schema(description = "Địa chỉ email")
    private String email;

    @Schema(description = "Số điện thoại")
    private String phone;

    @Schema(description = "Địa chỉ cụ thể")
    private String exactAddress;

    @Schema(description = "Phường/Xã")
    private String commune;

    @Schema(description = "Tỉnh/Thành phố")
    private String province;

    @Schema(description = "Thông tin tài khoản đăng nhập")
    private StaffAccountInfo account;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Thời gian tạo hồ sơ")
    private LocalDateTime createdAt;
}
