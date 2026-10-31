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
@Schema(description = "Yêu cầu phân công tài xế cho phương tiện")
public class AssignDriverRequest {

    @NotBlank(message = "Driver ID không được để trống")
    @JsonProperty("driver_id")
    @Schema(description = "ID của tài xế được phân công", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private String driverId;
}
