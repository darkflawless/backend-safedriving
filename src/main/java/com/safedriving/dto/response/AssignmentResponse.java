package com.safedriving.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {
    private String id;
    private String tripId;
    private String driverId;
    private String accountId;
    private LocalDateTime assignmentAt;

    private String driverName;
    private String vehiclePlate;
    private String vehicleId;
    private String routeCode;
    private String routeName;
}
