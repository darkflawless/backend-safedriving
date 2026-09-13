package com.safedriving.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {

    private String id;
    private String code;
    private String routeName;
    private Double distanceKm;
    private Integer standardDurationMin;
    private Boolean isActive;
    private String note;
}
