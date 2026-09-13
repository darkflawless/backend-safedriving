package com.safedriving.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.safedriving.entity.enums.ViolationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViolationResponse {

    private String id;
    private String driverId;
    private String incidentId;
    private String accountId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timeViolation;

    private ViolationType type;
    private BigDecimal penalty;
    private String note;
}
