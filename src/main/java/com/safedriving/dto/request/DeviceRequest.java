package com.safedriving.dto.request;

import com.safedriving.entity.enums.DeviceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu đăng ký hoặc cập nhật thiết bị")
public class DeviceRequest {

    @NotBlank(message = "Serial Number không được để trống")
    @Schema(description = "Số Serial của thiết bị", example = "SN123456789")
    private String serialNumber;

    @NotBlank(message = "MAC Address không được để trống")
    @Schema(description = "Địa chỉ MAC của thiết bị", example = "00:1B:44:11:3A:B7")
    private String macAddress;

    @Schema(description = "Trạng thái thiết bị", example = "ACTIVE")
    private DeviceStatus deviceStatus;

    @Schema(description = "Thời điểm kích hoạt thiết bị")
    private LocalDateTime startTime;
}
