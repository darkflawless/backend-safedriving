package com.safedriving.entity;

import com.safedriving.entity.enums.ViolationType;
import jakarta.persistence.Column;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "violation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Violation extends BaseUuidEntity {

    @NotNull(message = "Driver is required")
    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @NotNull(message = "Incident is required")
    @ManyToOne
    @JoinColumn(name = "incident_id")
    private Incident incident;

    @NotNull(message = "Account (reporter) is required")
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @NotNull(message = "Time violation is required")
    private LocalDateTime timeViolation;

    @NotNull(message = "Type is required")
    @Enumerated(EnumType.STRING)
    private ViolationType type;

    private BigDecimal penalty;

    @Column(columnDefinition = "TEXT")
    private String note;
}
