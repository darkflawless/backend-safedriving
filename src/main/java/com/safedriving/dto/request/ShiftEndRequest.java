package com.safedriving.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu kết thúc ca lái từ Mobile (Check-out ca lái)")
public class ShiftEndRequest {

    @NotBlank(message = "Shift ID không được để trống")
    @JsonProperty("shift_id")
    @Schema(description = "ID của ca lái cần chốt", example = "s9999999-1111-2222-3333-444455556666")
    private String shiftId;

    @JsonProperty("end_odometer")
    @Schema(description = "Số km công-tơ-mét khi chốt ca", example = "12510.0")
    private Double endOdometer;

    @JsonProperty("end_battery")
    @Schema(description = "Phần trăm pin điện thoại/thiết bị khi chốt ca", example = "72.0")
    private Double endBattery;
}
