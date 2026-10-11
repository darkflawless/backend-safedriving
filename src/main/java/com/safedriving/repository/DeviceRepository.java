package com.safedriving.repository;

import com.safedriving.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {

    List<Device> findByIsDeletedFalse();

    Optional<Device> findByIdAndIsDeletedFalse(String id);

    boolean existsBySerialNumberAndIsDeletedFalse(String serialNumber);

    boolean existsByMacAddressAndIsDeletedFalse(String macAddress);
    
    boolean existsBySerialNumberAndIdNotAndIsDeletedFalse(String serialNumber, String id);

    boolean existsByMacAddressAndIdNotAndIsDeletedFalse(String macAddress, String id);
}
