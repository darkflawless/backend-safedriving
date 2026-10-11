package com.safedriving.repository;

import com.safedriving.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, String> {

    boolean existsByCode(String code);

    Optional<Route> findByCode(String code);
}
