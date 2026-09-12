package com.safedriving.repository;

import com.safedriving.entity.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStop, String> {

    List<RouteStop> findByRouteIdOrderByOrderAsc(String routeId);

    List<RouteStop> findAllByOrderByRouteIdAscOrderAsc();

    boolean existsByRouteIdAndStopId(String routeId, String stopId);
}
