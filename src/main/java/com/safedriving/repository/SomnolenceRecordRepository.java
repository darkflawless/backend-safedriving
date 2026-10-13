package com.safedriving.repository;

import com.safedriving.entity.SomnolenceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SomnolenceRecordRepository extends JpaRepository<SomnolenceRecord, Long> {

    List<SomnolenceRecord> findByDriverId(String driverId);

    List<SomnolenceRecord> findByDriverIdOrderByRecordTimeDesc(String driverId);

    List<SomnolenceRecord> findAllByOrderByRecordTimeDesc();

    long countByTripId(String tripId);

    List<SomnolenceRecord> findByTripIdOrderByRecordTimeDesc(String tripId);
}

