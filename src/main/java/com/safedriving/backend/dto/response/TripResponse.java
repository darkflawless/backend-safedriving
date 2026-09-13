package com.safedriving.backend.dto.response;

import com.safedriving.backend.entity.enums.TripStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TripResponse {
    private String id;
    private String routeId;
    private TripStatus status;
    private LocalDateTime plannedStartTime;
    private LocalDateTime plannedEndTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer currentOrder;
}
