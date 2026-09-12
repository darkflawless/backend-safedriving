package com.safedriving.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequest {

    @NotBlank(message = "Mã tuyến đường không được để trống")
    private String code;

    @NotBlank(message = "Tên tuyến đường không được để trống")
    private String routeName;

    @NotNull(message = "Cự ly khoảng cách (km) không được để trống")
    private Double distanceKm;

    @NotNull(message = "Thời gian chạy tiêu chuẩn (phút) không được để trống")
    private Integer standardDurationMin;

    @Builder.Default
    private Boolean isActive = true;

    private String note;
}
