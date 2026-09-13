package com.safedriving.repository;

import com.safedriving.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, String> {

    Optional<Trip> findByIdAndIsDeletedFalse(String id);
}
