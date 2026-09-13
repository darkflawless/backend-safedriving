package com.safedriving.backend.dto.request;

import com.safedriving.backend.entity.enums.VehicleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class VehicleRequest {

    @NotBlank(message = "Biển số xe không được để trống")
    private String plateNumber;

    private String vin;

    private Integer capacity;

    @NotNull(message = "ID loại xe (vehicleTypeId) không được để trống")
    private Integer vehicleTypeId;

    private VehicleStatus status = VehicleStatus.AVAILABLE;

    private Double odometerKm;
}
