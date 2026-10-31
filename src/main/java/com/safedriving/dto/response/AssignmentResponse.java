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

    private String startLocation;
    private String endLocation;
    private Double distanceKm;
    private String status;
    private LocalDateTime plannedStartTime;
    private LocalDateTime plannedEndTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String note;
}
