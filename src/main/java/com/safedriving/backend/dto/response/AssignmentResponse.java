package com.safedriving.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AssignmentResponse {
    private String id;
    private String tripId;
    private String driverId;
    private String accountId;
    private LocalDateTime assignmentAt;
}
