package com.safedriving.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin chi tiết Giấy phép lái xe của tài xế")
public class LicenseResponse {

    @Schema(description = "ID của License (UUID)")
    private String id;

    @Schema(description = "Số GPLX")
    private String licenseNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày cấp")
    private LocalDate issueDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày hết hạn")
    private LocalDate licenseExpiry;

    @Schema(description = "Nơi cấp")
    private String placeOfIssue;

    @Schema(description = "Thông tin hạng bằng lái")
    private LicenseClassResponse licenseClass;
    
    @Schema(description = "ID của tài xế sở hữu GPLX này")
    private String driverId;
    
    @Schema(description = "Họ tên tài xế (tùy chọn để hiển thị nhanh)")
    private String driverName;
}
