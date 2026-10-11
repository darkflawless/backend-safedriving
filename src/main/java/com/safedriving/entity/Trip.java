package com.safedriving.entity;

import com.safedriving.entity.enums.TripStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Route is required")
    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @Enumerated(EnumType.STRING)
    @lombok.Builder.Default
    private TripStatus status = TripStatus.PLANNED;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @NotNull(message = "Planned start time is required")
    private LocalDateTime plannedStartTime;

    @NotNull(message = "Planned end time is required")
    private LocalDateTime plannedEndTime;

    @lombok.Builder.Default
    private Integer currentOrder = 0;

    @lombok.Builder.Default
    private Boolean isDeleted = false;
}
