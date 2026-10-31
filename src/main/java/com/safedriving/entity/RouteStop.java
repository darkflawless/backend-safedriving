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

@Entity
@Table(name = "route_stop")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RouteStop extends BaseUuidEntity {

    @NotNull(message = "Route is required")
    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @NotNull(message = "Stop is required")
    @ManyToOne
    @JoinColumn(name = "stop_id")
    private Stop stop;

    @NotNull(message = "Order is required")
    @Column(name = "stop_order") // "order" là từ khóa reserved trong SQL nên đổi tên cột
    private Integer order;
}
