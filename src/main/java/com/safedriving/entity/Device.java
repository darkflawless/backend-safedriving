package com.safedriving.entity;

import com.safedriving.entity.enums.DeviceStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "device")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Device extends BaseUuidEntity {

    @Enumerated(EnumType.STRING)
    @lombok.Builder.Default
    private DeviceStatus deviceStatus = DeviceStatus.ACTIVE;

    @lombok.Builder.Default
    private LocalDateTime startTime = LocalDateTime.now();
}
