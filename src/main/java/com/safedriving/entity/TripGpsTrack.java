package com.safedriving.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * Lưu trữ vệt tọa độ GPS theo thời gian của từng Chuyến đi (Trip)
 * Phục vụ vẽ lộ trình xe đã di chuyển thực tế trên bản đồ Web.
 */
@Entity
@Table(name = "trip_gps_track")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TripGpsTrack extends BaseUuidEntity {

    @NotNull(message = "Trip is required")
    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private Double speedKmh;

    @lombok.Builder.Default
    @Column(name = "recorded_at")
    private LocalDateTime recordedAt = LocalDateTime.now();
}
