package com.safedriving.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Yêu cầu tạo mới hoặc cập nhật hồ sơ tài xế")
public class DriverRequest {

    @NotNull(message = "Ngày tuyển dụng không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày tuyển dụng", example = "2023-01-15")
    private LocalDate hireDate;

    @Schema(description = "Đường dẫn ảnh đại diện/chân dung tài xế", example = "https://example.com/driver-photo.jpg")
    private String urlImage;

    @Builder.Default
    @Schema(description = "Trạng thái hoạt động của tài xế", example = "true")
    private Boolean isActive = true;

    @Schema(description = "Tham chiếu hồ sơ nhân sự dạng { id: ... }")
    private IdReference staff;

    @Schema(description = "ID hồ sơ nhân sự dạng phẳng (tùy chọn)")
    private String staffId;

    @Schema(description = "Tham chiếu xe được giao dạng { id: ... }")
    private IdReference vehicle;

    @Schema(description = "ID xe được giao dạng phẳng (tùy chọn)")
    private String vehicleId;



    public String resolveStaffId() {
        if (staff != null && staff.getId() != null && !staff.getId().isBlank()) {
            return staff.getId().trim();
        }
        if (staffId != null && !staffId.isBlank()) {
            return staffId.trim();
        }
        return null;
    }

    public String resolveVehicleId() {
        if (vehicle != null && vehicle.getId() != null && !vehicle.getId().isBlank()) {
            return vehicle.getId().trim();
        }
        if (vehicleId != null && !vehicleId.isBlank()) {
            return vehicleId.trim();
        }
        return null;
    }


}
