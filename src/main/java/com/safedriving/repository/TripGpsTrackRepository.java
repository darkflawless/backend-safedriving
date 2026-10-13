package com.safedriving.repository;

import com.safedriving.entity.TripGpsTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripGpsTrackRepository extends JpaRepository<TripGpsTrack, String> {

    List<TripGpsTrack> findByTripIdOrderByRecordedAtAsc(String tripId);

    long countByTripId(String tripId);
}
