package com.safedriving.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleTypeRequest {
    @NotBlank(message = "Mã loại xe không được để trống")
    private String code;

    @NotBlank(message = "Tên loại xe không được để trống")
    private String name;

    @NotNull(message = "Sức chứa (số chỗ ngồi) không được để trống")
    @Min(value = 1, message = "Sức chứa tối thiểu phải từ 1 chỗ trở lên")
    private Integer capacity;
}
