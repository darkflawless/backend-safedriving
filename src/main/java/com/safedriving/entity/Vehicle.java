package com.safedriving.entity;

import com.safedriving.entity.enums.VehicleStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "vehicle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Vehicle extends BaseUuidEntity {

    @NotBlank(message = "Plate number is required")
    private String plateNumber;

    private String vin;

    private Integer capacity;

    @NotNull(message = "Vehicle type is required")
    @ManyToOne
    @JoinColumn(name = "vehicle_type_id")
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @lombok.Builder.Default
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @ManyToOne
    @JoinColumn(name = "device_id")
    private Device device;

    private Double odometerKm;

    @lombok.Builder.Default
    private Boolean isDeleted = false;
}
