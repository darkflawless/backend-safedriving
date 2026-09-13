package com.safedriving.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SomnolenceRecordRequest {

    @NotBlank(message = "ID tài xế (driverId) không được để trống")
    private String driverId;

    private String vehicleId;
    private LocalDateTime recordTime = LocalDateTime.now();
    private Integer drowsinessLevel;
    private String imageUrl;
    private String notes;
}
