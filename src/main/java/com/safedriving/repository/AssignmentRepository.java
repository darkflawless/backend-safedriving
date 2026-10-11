package com.safedriving.repository;

import com.safedriving.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, String> {
    List<Assignment> findAllByIsDeletedFalse();
    Optional<Assignment> findByIdAndIsDeletedFalse(String id);

    @Query("SELECT a FROM Assignment a WHERE a.driver.id = :driverId AND a.isDeleted = false")
    List<Assignment> findByDriverIdAndIsDeletedFalse(@Param("driverId") String driverId);
}
