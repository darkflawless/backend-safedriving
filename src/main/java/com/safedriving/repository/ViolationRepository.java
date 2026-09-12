package com.safedriving.repository;

import com.safedriving.entity.Violation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ViolationRepository extends JpaRepository<Violation, String> {

    List<Violation> findByDriverIdOrderByTimeViolationDesc(String driverId);

    List<Violation> findAllByOrderByTimeViolationDesc();
}
