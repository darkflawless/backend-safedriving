package com.safedriving.repository;

import com.safedriving.entity.Trip;
import com.safedriving.entity.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, String> {

    Optional<Trip> findByIdAndIsDeletedFalse(String id);

    List<Trip> findAllByIsDeletedFalseOrderByStartTimeDesc();

    List<Trip> findByStatusAndIsDeletedFalseOrderByStartTimeDesc(TripStatus status);

    List<Trip> findByDriverIdAndIsDeletedFalseOrderByStartTimeDesc(String driverId);

    List<Trip> findByVehicleIdAndIsDeletedFalseOrderByStartTimeDesc(String vehicleId);

    Optional<Trip> findFirstByDriverIdAndStatusAndIsDeletedFalseOrderByStartTimeDesc(String driverId, TripStatus status);

    Optional<Trip> findFirstByVehicleIdAndStatusAndIsDeletedFalseOrderByStartTimeDesc(String vehicleId, TripStatus status);
}
