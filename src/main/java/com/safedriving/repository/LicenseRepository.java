package com.safedriving.repository;

import com.safedriving.entity.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, String> {

    List<License> findByIsDeletedFalse();

    Optional<License> findByIdAndIsDeletedFalse(String id);

    List<License> findByDriverIdAndIsDeletedFalse(String driverId);

    boolean existsByLicenseNoAndIsDeletedFalse(String licenseNo);

    boolean existsByLicenseNoAndIdNotAndIsDeletedFalse(String licenseNo, String id);
}
