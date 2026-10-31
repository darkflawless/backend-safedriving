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

    @JsonProperty("vehicle_type_id")
    private Short vehicleTypeId;

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

    // Dual getters for seamless frontend camelCase access
    @JsonProperty("plateNumber")
    public String getPlateNumberCamel() {
        return plateNumber;
    }

    @JsonProperty("vehicleTypeId")
    public Short getVehicleTypeIdCamel() {
        return vehicleTypeId;
    }

    @JsonProperty("vehicleTypeName")
    public String getVehicleTypeNameCamel() {
        return vehicleTypeName;
    }

    @JsonProperty("odometerKm")
    public Double getOdometerKmCamel() {
        return odometerKm;
    }

    @JsonProperty("deviceId")
    public String getDeviceIdCamel() {
        return deviceId;
    }

    @JsonProperty("assignedDriverId")
    public String getAssignedDriverIdCamel() {
        return assignedDriverId;
    }

    @JsonProperty("assignedDriverName")
    public String getAssignedDriverNameCamel() {
        return assignedDriverName;
    }
}
