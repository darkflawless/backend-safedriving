package com.safedriving.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.safedriving.entity.enums.HazardType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Thực thể lưu trữ các Điểm đen nguy cơ tai nạn giao thông (Accident Blackspot / Geofence Hazard Zone).
 * Phục vụ khoanh vùng bán kính nguy hiểm (mặc định 200m) và bắn cảnh báo khi phương tiện tiến vào vùng.
 */
@Entity
@Table(name = "accident_blackspot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccidentBlackspot extends BaseUuidEntity {

    @NotBlank(message = "Tên điểm đen không được để trống")
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull(message = "Vĩ độ (latitude) là bắt buộc")
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @NotNull(message = "Kinh độ (longitude) là bắt buộc")
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Builder.Default
    @NotNull(message = "Bán kính cảnh báo là bắt buộc")
    @Column(name = "radius_meters", nullable = false)
    private Double radiusMeters = 200.0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "hazard_type", nullable = false)
    private HazardType hazardType = HazardType.ACCIDENT_PRONE;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "feedback_count")
    private Integer feedbackCount = 0;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = true)
    private Route route;

    public String getRouteId() {
        return route != null ? route.getId() : null;
    }

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
