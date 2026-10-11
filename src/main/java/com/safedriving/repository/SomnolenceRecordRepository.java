package com.safedriving.repository;

import com.safedriving.entity.SomnolenceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SomnolenceRecordRepository extends JpaRepository<SomnolenceRecord, Long> {
    @Query("SELECT s FROM SomnolenceRecord s WHERE s.driver.id = :driverId")
    List<SomnolenceRecord> findByDriverId(@Param("driverId") String driverId);
}
