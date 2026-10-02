package com.safedriving.dto.response;

import com.safedriving.entity.enums.VehicleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin xe được giao cho tài xế")
public class DriverVehicleInfo {

    @Schema(description = "ID phương tiện (UUID)")
    private String id;

    @Schema(description = "Biển số xe")
    private String plateNumber;

    @Schema(description = "Số khung xe (VIN)")
    private String vin;

    @Schema(description = "Sức chứa (số chỗ hoặc tấn)")
    private Integer capacity;

    @Schema(description = "Trạng thái xe")
    private VehicleStatus status;
}
