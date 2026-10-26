package com.safedriving.repository;

import com.safedriving.entity.AccidentBlackspot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccidentBlackspotRepository extends JpaRepository<AccidentBlackspot, String> {

    List<AccidentBlackspot> findByIsActiveTrueAndIsDeletedFalse();

    @org.springframework.data.jpa.repository.Query("SELECT b FROM AccidentBlackspot b WHERE b.route.id = :routeId AND b.isActive = true AND b.isDeleted = false")
    List<AccidentBlackspot> findByRouteIdAndIsActiveTrueAndIsDeletedFalse(@org.springframework.data.repository.query.Param("routeId") String routeId);

    Optional<AccidentBlackspot> findByIdAndIsDeletedFalse(String id);
}
