package com.safedriving.entity;

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

@Entity
@Table(name = "assignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Assignment extends BaseUuidEntity {

    @lombok.Builder.Default
    private LocalDateTime assignmentAt = LocalDateTime.now();

    @NotNull(message = "Trip is required")
    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @NotNull(message = "Account (assigner) is required")
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @NotNull(message = "Driver is required")
    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @lombok.Builder.Default
    private Boolean isDeleted = false;
}
