package com.safedriving.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.safedriving.entity.enums.VehicleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO yêu cầu tạo hoặc cập nhật phương tiện.
 *
 * @author phanthuy0811
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo hoặc cập nhật phương tiện")
public class VehicleRequest {

    @NotBlank(message = "Biển số xe không được để trống")
    @JsonAlias({"plate_number", "plateNumber"})
    @JsonProperty("plate_number")
    @Schema(description = "Biển số xe", example = "30A-999.88")
    private String plateNumber;

    @Schema(description = "Mã số khung xe (VIN)", example = "VF8VN2023000123")
    private String vin;

    @Schema(description = "Sức chứa hành khách / chỗ ngồi", example = "5")
    private Integer capacity;

    @NotNull(message = "ID loại xe không được để trống")
    @JsonAlias({"vehicle_type_id", "vehicleTypeId"})
    @JsonProperty("vehicle_type_id")
    @Schema(description = "ID danh mục loại xe", example = "1")
    private Short vehicleTypeId;

    @Builder.Default
    @Schema(description = "Trạng thái hoạt động", example = "AVAILABLE")
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @JsonAlias({"odometer_km", "odometerKm"})
    @JsonProperty("odometer_km")
    @Schema(description = "Số ODO hiển thị (km)", example = "15420.5")
    private Double odometerKm;

    @JsonAlias({"device_id", "deviceId"})
    @JsonProperty("device_id")
    @Schema(description = "ID thiết bị giám sát trên xe (tuỳ chọn)")
    private String deviceId;
}
