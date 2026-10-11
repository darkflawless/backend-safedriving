package com.safedriving.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SomnolenceRecordResponse {
    private Long id;
    private String driverId;
    private LocalDateTime recordTime;
    private String notes;
}
