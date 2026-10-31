package com.safedriving.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Bảng danh mục loại xe - id gán thủ công (seed data).
 * VD: BUS29, BUS45, SLEEPER...
 */
@Entity
@Table(name = "vehicle_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleType {

    @Id
    private Short id;

    @NotBlank(message = "Code is required")
    @Column(unique = true)
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    private Integer capacity;
}
