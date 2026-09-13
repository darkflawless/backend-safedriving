package com.safedriving.backend.dto.request;

import com.safedriving.backend.entity.enums.IncidentSeverity;
import com.safedriving.backend.entity.enums.IncidentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IncidentRequest {

    @NotBlank(message = "ID chuyến đi (tripId) không được để trống")
    private String tripId;

    @NotNull(message = "Thời gian xảy ra sự cố không được để trống")
    private LocalDateTime timeIncident;

    @NotNull(message = "Loại sự cố không được để trống")
    private IncidentType type;

    private String description;

    @NotNull(message = "Mức độ nghiêm trọng không được để trống")
    private IncidentSeverity severity;
}
