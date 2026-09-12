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

@Entity
@Table(name = "trip_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TripProgress extends BaseUuidEntity {

    @NotNull(message = "Trip is required")
    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @Column(name = "stop_order")
    private Integer order;

    @lombok.Builder.Default
    private LocalDateTime arrive = LocalDateTime.now();

    @Column(name = "leave_time") // "leave" là từ khóa reserved trong SQL
    private LocalDateTime leave;
}
