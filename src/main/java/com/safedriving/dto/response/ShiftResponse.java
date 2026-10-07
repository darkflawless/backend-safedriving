package com.safedriving.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.safedriving.entity.enums.ShiftStatus;
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
@Schema(description = "Thông tin chi tiết ca lái và cấu hình MQTT")
public class ShiftResponse {

    @JsonProperty("shift_id")
    @Schema(description = "ID của ca lái")
    private String shiftId;

    @JsonProperty("driver_id")
    @Schema(description = "ID của tài xế")
    private String driverId;

    @Schema(description = "Thông tin xe phân công")
    private VehicleBasicResponse vehicle;

    @JsonProperty("start_time")
    @Schema(description = "Thời gian bắt đầu ca lái")
    private LocalDateTime startTime;

    @JsonProperty("end_time")
    @Schema(description = "Thời gian kết thúc ca lái")
    private LocalDateTime endTime;

    @Schema(description = "Trạng thái ca lái", example = "ACTIVE")
    private ShiftStatus status;

    @JsonProperty("start_odometer")
    @Schema(description = "Odometer bắt đầu")
    private Double startOdometer;

    @JsonProperty("end_odometer")
    @Schema(description = "Odometer kết thúc")
    private Double endOdometer;

    @JsonProperty("mqtt_config")
    @Schema(description = "Cấu hình kết nối MQTT cho Mobile")
    private MqttConfigResponse mqttConfig;
}
