package com.safedriving.dto.request;

import com.safedriving.entity.enums.TripStatus;
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
public class TripRequest {

    @NotBlank(message = "Start location is required")
    private String startLocation;

    @NotBlank(message = "End location is required")
    private String endLocation;

    private String vehicleId;

    private String driverId;

    private LocalDateTime plannedStartTime;

    private LocalDateTime plannedEndTime;

    private TripStatus status;

    private Double distanceKm;

    private String note;

    private String routeId; // Tùy chọn nếu muốn liên kết Route cũ
}
