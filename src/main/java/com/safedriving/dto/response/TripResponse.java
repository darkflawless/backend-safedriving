package com.safedriving.dto.response;

import com.safedriving.entity.enums.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripResponse {

    private String id;

    private String startLocation;

    private String endLocation;

    private String vehicleId;

    private String vehiclePlateNumber;

    private String vehicleTypeName;

    private String driverId;

    private String driverName;

    private String driverPhone;

    private Double distanceKm;

    private TripStatus status;

    private LocalDateTime plannedStartTime;

    private LocalDateTime plannedEndTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String note;

    private String routeId;

    private String routeName;

    // Chỉ số an toàn thu thập được trong chuyến đi
    private Long somnolenceCount;

    private Long incidentCount;

    private Long gpsTrackCount;

    private LocalDateTime createdAt;
}
