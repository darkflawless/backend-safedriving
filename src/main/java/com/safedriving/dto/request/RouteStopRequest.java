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
public class RouteStopRequest {

    @NotBlank(message = "ID tuyến đường không được để trống")
    private String routeId;

    @NotBlank(message = "ID điểm dừng không được để trống")
    private String stopId;

    @NotNull(message = "Thứ tự điểm dừng không được để trống")
    private Integer order;
}
