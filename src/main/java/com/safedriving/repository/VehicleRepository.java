package com.safedriving.repository;

import com.safedriving.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {
    List<Vehicle> findAllByIsDeletedFalse();
    Optional<Vehicle> findByIdAndIsDeletedFalse(String id);
    boolean existsByPlateNumberAndIsDeletedFalse(String plateNumber);
}
