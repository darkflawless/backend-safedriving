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
@Schema(description = "Thông tin phản hồi hồ sơ tài xế")
public class DriverResponse {

    @Schema(description = "ID tài xế (UUID)")
    private String id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày tuyển dụng")
    private LocalDate hireDate;

    @Schema(description = "Đường dẫn ảnh đại diện")
    private String urlImage;

    @Schema(description = "Trạng thái hoạt động")
    private Boolean isActive;

    @Schema(description = "Hồ sơ nhân sự chi tiết của tài xế")
    private StaffResponse staff;

    @Schema(description = "Phương tiện được giao cho tài xế")
    private DriverVehicleInfo vehicle;

    @Schema(description = "Hạng bằng lái xe của tài xế")
    private LicenseClassResponse licenseClass;
}
