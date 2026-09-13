package com.safedriving.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignmentRequest {

    @NotBlank(message = "ID chuyến đi (tripId) không được để trống")
    private String tripId;

    @NotBlank(message = "ID tài xế (driverId) không được để trống")
    private String driverId;

    @NotBlank(message = "ID người phân công (accountId) không được để trống")
    private String accountId;
}
