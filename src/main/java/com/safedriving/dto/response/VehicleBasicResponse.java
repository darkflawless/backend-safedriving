package com.safedriving.dto.response;

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
@Schema(description = "Thông tin cơ bản của phương tiện")
public class VehicleBasicResponse {

    @Schema(description = "ID của phương tiện", example = "f8a12345-0000-0000-0000-000000000001")
    private String id;

    @JsonProperty("plate_number")
    @Schema(description = "Biển số xe", example = "30G-456.78")
    private String plateNumber;

    @Schema(description = "Dòng xe / Model", example = "VinFast VF8")
    private String model;

    @Schema(description = "Trạng thái xe", example = "IN_USE")
    private String status;
}
