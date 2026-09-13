package com.safedriving.backend.dto.response;

import com.safedriving.backend.entity.enums.IncidentSeverity;
import com.safedriving.backend.entity.enums.IncidentType;
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
}
