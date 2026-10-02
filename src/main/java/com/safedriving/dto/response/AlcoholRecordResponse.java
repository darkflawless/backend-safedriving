package com.safedriving.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Thông tin bản ghi nồng độ cồn")
public class AlcoholRecordResponse {

    @Schema(description = "ID bản ghi")
    private Long id;

    @Schema(description = "ID tài xế")
    private String driverId;

    @Schema(description = "Tên tài xế")
    private String driverName;

    @Schema(description = "Chỉ số nồng độ cồn (mg/l khí thở)")
    private BigDecimal alcoholLevel;

    @Schema(description = "Thời điểm đo đạc")
    private LocalDateTime measurementTime;

    @Schema(description = "Ghi chú")
    private String notes;
}
