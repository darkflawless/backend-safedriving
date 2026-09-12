package com.safedriving.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.safedriving.entity.enums.ViolationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ViolationRequest {

    @NotBlank(message = "ID tài xế không được để trống")
    private String driverId;

    @NotBlank(message = "ID sự cố không được để trống")
    private String incidentId;

    @NotBlank(message = "ID tài khoản người lập biên bản không được để trống")
    private String accountId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timeViolation;

    @NotNull(message = "Loại vi phạm không được để trống")
    private ViolationType type;

    private BigDecimal penalty;

    private String note;
}
