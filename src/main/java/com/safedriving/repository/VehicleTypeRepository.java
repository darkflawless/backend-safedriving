package com.safedriving.repository;

import com.safedriving.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository thao tác bảng vehicle_types.
 *
 * @author phanthuy0811
 */
@Repository
public interface VehicleTypeRepository extends JpaRepository<VehicleType, Short> {

    Optional<VehicleType> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Short id);

    @Query("SELECT MAX(v.id) FROM VehicleType v")
    Optional<Short> findMaxId();
}
