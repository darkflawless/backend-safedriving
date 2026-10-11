package com.safedriving.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu bắt đầu ca lái từ Mobile (Check-in ca lái)")
public class ShiftStartRequest {

    @JsonProperty("start_odometer")
    @Schema(description = "Số km công-tơ-mét khi bắt đầu ca lái", example = "12450.5")
    private Double startOdometer;

    @JsonProperty("start_battery")
    @Schema(description = "Phần trăm pin điện thoại/thiết bị khi bắt đầu ca", example = "85.0")
    private Double startBattery;
}
