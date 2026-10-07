package com.safedriving.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.safedriving.entity.enums.IncidentSeverity;
import com.safedriving.entity.enums.SafetyAlertType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chi tiết sự kiện cảnh báo an toàn kèm dữ liệu sinh trắc Edge AI")
public class SafetyAlertDetailResponse {

    private String id;

    @JsonProperty("vehicle_id")
    private String vehicleId;

    @JsonProperty("plate_number")
    private String plateNumber;

    @JsonProperty("driver_id")
    private String driverId;

    @JsonProperty("driver_name")
    private String driverName;

    @JsonProperty("alert_type")
    private SafetyAlertType alertType;

    private IncidentSeverity severity;

    private LocalDateTime timestamp;

    private Double latitude;
    private Double longitude;

    @JsonProperty("speed_kmh")
    private Double speedKmh;

    @JsonProperty("ear_value")
    private Float earValue;

    @JsonProperty("mar_value")
    private Float marValue;

    @JsonProperty("pitch_value")
    private Float pitchValue;

    @JsonProperty("yaw_value")
    private Float yawValue;

    @JsonProperty("perclos_value")
    private Float perclosValue;

    @JsonProperty("snapshot_url")
    private String snapshotUrl;
}
