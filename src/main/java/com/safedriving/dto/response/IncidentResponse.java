package com.safedriving.dto.response;

import com.safedriving.entity.enums.IncidentSeverity;
import com.safedriving.entity.enums.IncidentType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class IncidentResponse {
    private String id;
    private String tripId;
    private LocalDateTime timeIncident;
    private IncidentType type;
    private String description;
    private IncidentSeverity severity;

    // Thông tin chuyến đi để hiển thị trên UI
    private String tripStartLocation;
    private String tripEndLocation;
    private LocalDateTime tripPlannedStartTime;
}
