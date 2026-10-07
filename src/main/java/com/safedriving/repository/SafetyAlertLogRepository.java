package com.safedriving.repository;

import com.safedriving.entity.SafetyAlertLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SafetyAlertLogRepository extends JpaRepository<SafetyAlertLog, String> {

    List<SafetyAlertLog> findByVehicleIdOrderByTimestampDesc(String vehicleId);

    List<SafetyAlertLog> findByShiftIdOrderByTimestampDesc(String shiftId);

    List<SafetyAlertLog> findByDriverIdOrderByTimestampDesc(String driverId);
}
