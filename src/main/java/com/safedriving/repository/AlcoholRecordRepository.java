package com.safedriving.repository;

import com.safedriving.entity.AlcoholRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlcoholRecordRepository extends JpaRepository<AlcoholRecord, Long> {

    List<AlcoholRecord> findByDriverIdOrderByMeasurementTimeDesc(String driverId);
}
