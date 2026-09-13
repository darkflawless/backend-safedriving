package com.safedriving.backend.repository;

import com.safedriving.backend.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, String> {
    List<Assignment> findAllByIsDeletedFalse();
    Optional<Assignment> findByIdAndIsDeletedFalse(String id);
    List<Assignment> findByDriverIdAndIsDeletedFalse(String driverId);
}
