package com.safedriving.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO yêu cầu tạo hoặc cập nhật loại xe.
 *
 * @author phanthuy0811
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo hoặc cập nhật loại xe")
public class VehicleTypeRequest {

    @NotBlank(message = "Mã loại xe không được để trống")
    @Schema(description = "Mã loại xe", example = "BUS29")
    private String code;

    @NotBlank(message = "Tên loại xe không được để trống")
    @Schema(description = "Tên loại xe", example = "Xe Khách 29 chỗ")
    private String name;

    @NotNull(message = "Sức chứa không được để trống")
    @Min(value = 1, message = "Sức chứa tối thiểu là 1")
    @Schema(description = "Sức chứa hành khách", example = "29")
    private Integer capacity;
}
