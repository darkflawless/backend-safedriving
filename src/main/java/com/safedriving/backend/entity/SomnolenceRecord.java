package com.safedriving.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "somnolence_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SomnolenceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "driver_id", length = 36, nullable = false)
    private String driverId;

    @Column(name = "vehicle_id", length = 36)
    private String vehicleId;

    @Column(name = "record_time", nullable = false)
    @Builder.Default
    private LocalDateTime recordTime = LocalDateTime.now();

    @Column(name = "drowsiness_level")
    private Integer drowsinessLevel; // Mức độ ngủ gật (1 - 5)

    @Column(name = "image_url")
    private String imageUrl; // URL ảnh bằng chứng từ AI Camera

    @Column(name = "notes")
    private String notes;
}
