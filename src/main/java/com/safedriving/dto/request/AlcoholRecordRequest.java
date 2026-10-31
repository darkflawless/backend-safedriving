package com.safedriving.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu ghi nhận dữ liệu nồng độ cồn")
public class AlcoholRecordRequest {

    @NotNull(message = "ID tài xế không được để trống")
    @Schema(description = "ID của tài xế", example = "driver-uuid")
    private String driverId;

    @NotNull(message = "Nồng độ cồn không được để trống")
    @PositiveOrZero(message = "Nồng độ cồn không được âm")
    @Schema(description = "Chỉ số nồng độ cồn đo được (mg/l khí thở)", example = "0.0")
    private BigDecimal alcoholLevel;

    @Schema(description = "Thời điểm đo đạc (Bỏ trống sẽ lấy giờ hiện tại)")
    private LocalDateTime measurementTime;

    @Schema(description = "Ghi chú thêm", example = "Tài xế tỉnh táo, đủ điều kiện lái xe")
    private String notes;
}
