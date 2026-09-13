package com.safedriving.backend.dto.request;

import com.safedriving.backend.entity.enums.TripStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TripRequest {

    @NotBlank(message = "ID tuyến đường (routeId) không được để trống")
    private String routeId;

    @NotNull(message = "Thời gian xuất phát dự kiến không được để trống")
    private LocalDateTime plannedStartTime;

    @NotNull(message = "Thời gian đến dự kiến không được để trống")
    private LocalDateTime plannedEndTime;

    private TripStatus status = TripStatus.PLANNED;
}
