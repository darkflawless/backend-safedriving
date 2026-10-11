package com.safedriving.repository;

import com.safedriving.entity.DeviceHeartbeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceHeartbeatRepository extends JpaRepository<DeviceHeartbeat, String> {

    List<DeviceHeartbeat> findByDeviceIdOrderByTimestampDesc(String deviceId);

    List<DeviceHeartbeat> findByVehicleIdOrderByTimestampDesc(String vehicleId);
}
