package com.safedriving.repository;

import com.safedriving.entity.ShiftSession;
import com.safedriving.entity.enums.ShiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftSessionRepository extends JpaRepository<ShiftSession, String> {

    Optional<ShiftSession> findByDriverIdAndStatus(String driverId, ShiftStatus status);

    Optional<ShiftSession> findByVehicleIdAndStatus(String vehicleId, ShiftStatus status);

    List<ShiftSession> findByDriverIdOrderByStartTimeDesc(String driverId);

    List<ShiftSession> findByStatus(ShiftStatus status);
}
