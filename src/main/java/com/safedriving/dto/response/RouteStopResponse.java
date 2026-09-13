package com.safedriving.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteStopResponse {

    private String id;
    private String routeId;
    private String routeCode;
    private String routeName;
    private StopResponse stop;
    private Integer order;
}
