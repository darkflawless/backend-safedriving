package com.safedriving.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@Schema(description = "Thông tin chi tiết phương tiện")
public class VehicleResponse {

    private String id;

    @JsonProperty("plate_number")
    private String plateNumber;

    private String vin;

    private Integer capacity;

    @JsonProperty("vehicle_type_name")
    private String vehicleTypeName;

    private VehicleStatus status;

    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("odometer_km")
    private Double odometerKm;

    @JsonProperty("assigned_driver_id")
    private String assignedDriverId;

    @JsonProperty("assigned_driver_name")
    private String assignedDriverName;
}
