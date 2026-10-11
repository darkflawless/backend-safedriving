package com.safedriving.repository;

import com.safedriving.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.safedriving.entity.enums.VehicleStatus;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {

    Optional<Vehicle> findByIdAndIsDeletedFalse(String id);

    boolean existsByIdAndIsDeletedFalse(String id);

    List<Vehicle> findByIsDeletedFalse();

    List<Vehicle> findByStatusAndIsDeletedFalse(VehicleStatus status);

    Optional<Vehicle> findByPlateNumberAndIsDeletedFalse(String plateNumber);

    boolean existsByPlateNumberAndIsDeletedFalse(String plateNumber);

    boolean existsByPlateNumberAndIdNotAndIsDeletedFalse(String plateNumber, String id);
}
