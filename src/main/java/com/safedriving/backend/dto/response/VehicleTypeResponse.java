package com.safedriving.backend.dto.response;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class VehicleTypeResponse {
    private Integer id;
    private String code;
    private String name;
    private Integer capacity;
}