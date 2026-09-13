package com.safedriving.backend.repository;

import com.safedriving.backend.entity.SomnolenceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SomnolenceRecordRepository extends JpaRepository<SomnolenceRecord, Long> {
    List<SomnolenceRecord> findByDriverId(String driverId);
}
