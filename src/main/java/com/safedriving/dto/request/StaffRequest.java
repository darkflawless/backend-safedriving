package com.safedriving.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.safedriving.entity.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo mới hoặc cập nhật hồ sơ nhân sự")
public class StaffRequest {

    @NotBlank(message = "Họ không được để trống")
    @Schema(description = "Họ và tên đệm", example = "Van A")
    private String firstName;

    @NotBlank(message = "Tên không được để trống")
    @Schema(description = "Tên chính", example = "Nguyen")
    private String lastName;

    @NotNull(message = "Ngày sinh không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày sinh", example = "1995-05-20")
    private LocalDate dateOfBirth;

    @Schema(description = "Giới tính (MALE, FEMALE, OTHER)", example = "MALE")
    private Gender gender;

    @Email(message = "Email không đúng định dạng")
    @Schema(description = "Địa chỉ email", example = "vana@example.com")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Schema(description = "Số điện thoại liên hệ", example = "0901234567")
    private String phone;

    @Schema(description = "Đối tượng liên kết địa chỉ dạng { id: ... }")
    private IdReference address;

    @Schema(description = "ID địa chỉ dạng phẳng (tùy chọn)")
    private String addressId;

    @Schema(description = "Đối tượng liên kết tài khoản dạng { id: ... }")
    private IdReference account;

    @Schema(description = "ID tài khoản dạng phẳng (tùy chọn)")
    private String accountId;

    public String resolveAddressId() {
        if (address != null && address.getId() != null && !address.getId().isBlank()) {
            return address.getId().trim();
        }
        if (addressId != null && !addressId.isBlank()) {
            return addressId.trim();
        }
        return null;
    }

    public String resolveAccountId() {
        if (account != null && account.getId() != null && !account.getId().isBlank()) {
            return account.getId().trim();
        }
        if (accountId != null && !accountId.isBlank()) {
            return accountId.trim();
        }
        return null;
    }
}
