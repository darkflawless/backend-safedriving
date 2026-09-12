package com.safedriving.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "route")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Route extends BaseUuidEntity {

    @NotBlank(message = "Route code is required")
    private String code;

    @NotBlank(message = "Route name is required")
    private String routeName;

    @NotNull(message = "Distance is required")
    private Double distanceKm;

    @NotNull(message = "Standard duration is required")
    private Integer standardDurationMin;

    @lombok.Builder.Default
    private Boolean isActive = true;

    private String note;
}
