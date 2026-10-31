package com.safedriving.entity;

import com.safedriving.entity.enums.IncidentSeverity;
import com.safedriving.entity.enums.SafetyAlertType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "safety_alert_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SafetyAlertLog extends BaseUuidEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    private ShiftSession shift;

    @NotNull(message = "Vehicle is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false)
    private SafetyAlertType alertType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentSeverity severity;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime timestamp;

    private Double latitude;
    private Double longitude;
    private Double speedKmh;

    private Float earValue;
    private Float marValue;
    private Float pitchValue;
    private Float yawValue;
    private Float perclosValue;

    @Column(name = "snapshot_url", columnDefinition = "TEXT")
    private String snapshotUrl;

    @Builder.Default
    @Column(name = "is_synced")
    private Boolean isSynced = true;
}
