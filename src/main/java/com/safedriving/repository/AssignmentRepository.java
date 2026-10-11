package com.safedriving.repository;

import com.safedriving.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, String> {

    List<Assignment> findByTripIdAndIsDeletedFalse(String tripId);

    List<Assignment> findByDriverIdAndIsDeletedFalse(String driverId);
}
