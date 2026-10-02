package com.safedriving.dto.response;

import com.safedriving.entity.enums.DeviceStatus;
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
@Schema(description = "Thông tin thiết bị")
public class DeviceResponse {

    @Schema(description = "ID thiết bị (UUID)")
    private String id;

    @Schema(description = "Số Serial")
    private String serialNumber;

    @Schema(description = "Địa chỉ MAC")
    private String macAddress;

    @Schema(description = "Trạng thái thiết bị")
    private DeviceStatus deviceStatus;

    @Schema(description = "Thời điểm kích hoạt thiết bị")
    private LocalDateTime startTime;
}
