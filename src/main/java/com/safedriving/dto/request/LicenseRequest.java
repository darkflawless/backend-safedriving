package com.safedriving.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Yêu cầu tạo mới hoặc cập nhật giấy phép lái xe (License)")
public class LicenseRequest {

    @NotBlank(message = "Số GPLX không được để trống")
    @Schema(description = "Số Giấy phép lái xe", example = "123456789012")
    private String licenseNo;

    @NotNull(message = "Ngày cấp không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày cấp GPLX", example = "2020-01-01")
    private LocalDate issueDate;

    @NotNull(message = "Ngày hết hạn không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày hết hạn GPLX", example = "2030-01-01")
    private LocalDate licenseExpiry;

    @NotBlank(message = "Nơi cấp không được để trống")
    @Schema(description = "Nơi cấp GPLX", example = "Sở GTVT Hà Nội")
    private String placeOfIssue;

    @Schema(description = "Tham chiếu ID của tài xế", example = "driver-uuid")
    private String driverId;

    @Schema(description = "Tham chiếu hạng GPLX dạng { id: ... }")
    private LicenseClassReference licenseClass;

    @Schema(description = "ID hạng GPLX dạng phẳng (tùy chọn)")
    private Short licenseClassId;

    public Short resolveLicenseClassId() {
        if (licenseClass != null && licenseClass.getShortId() != null) {
            return licenseClass.getShortId();
        }
        return licenseClassId;
    }
}
