package com.safedriving.entity;

import com.safedriving.entity.enums.IncidentSeverity;
import com.safedriving.entity.enums.IncidentType;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "incident")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Incident extends BaseUuidEntity {

    @NotNull(message = "Trip is required")
    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @NotNull(message = "Time incident is required")
    private LocalDateTime timeIncident;

    @NotNull(message = "Type is required")
    @Enumerated(EnumType.STRING)
    private IncidentType type;

    private String description;

    @NotNull(message = "Severity is required")
    @Enumerated(EnumType.STRING)
    private IncidentSeverity severity;
}
