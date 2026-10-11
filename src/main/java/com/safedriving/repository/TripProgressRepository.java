package com.safedriving.repository;

import com.safedriving.entity.TripProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripProgressRepository extends JpaRepository<TripProgress, String> {

    List<TripProgress> findByTripIdOrderByOrderAsc(String tripId);

    List<TripProgress> findAllByOrderByArriveDesc();
}
