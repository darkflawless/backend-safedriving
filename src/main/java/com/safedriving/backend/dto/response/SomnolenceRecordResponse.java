package com.safedriving.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SomnolenceRecordResponse {
    private Long id;
    private String driverId;
    private String vehicleId;
    private LocalDateTime recordTime;
    private Integer drowsinessLevel;
    private String imageUrl;
    private String notes;
}
