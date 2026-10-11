package com.safedriving.entity;

import com.safedriving.entity.enums.TripStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "trip")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Trip extends BaseUuidEntity {

    /**
     * Tuyến đường (tùy chọn / nullable, hệ thống chuyển sang quản lý theo Chặng A -> B thực tế)
     */
    @ManyToOne
    @JoinColumn(name = "route_id", nullable = true)
    private Route route;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Column(name = "start_location")
    private String startLocation;

    @Column(name = "end_location")
    private String endLocation;

    @lombok.Builder.Default
    @Column(name = "distance_km")
    private Double distanceKm = 0.0;

    @Enumerated(EnumType.STRING)
    @lombok.Builder.Default
    private TripStatus status = TripStatus.PLANNED;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime plannedStartTime;

    private LocalDateTime plannedEndTime;

    @lombok.Builder.Default
    private Integer currentOrder = 0;

    private String note;

    @lombok.Builder.Default
    private Boolean isDeleted = false;
}
