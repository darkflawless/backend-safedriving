package com.safedriving.backend.dto.response;

import com.safedriving.backend.entity.enums.VehicleStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VehicleResponse {
    private String id;
    private String plateNumber;
    private String vin;
    private Integer capacity;
    private Integer vehicleTypeId;
    private String vehicleTypeName;
    private VehicleStatus status;
    private Double odometerKm;
}
