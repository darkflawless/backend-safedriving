package com.safedriving.service;

import com.safedriving.dto.request.TripGpsTrackRequest;
import com.safedriving.dto.request.TripRequest;
import com.safedriving.dto.response.TripGpsTrackResponse;
import com.safedriving.dto.response.TripResponse;
import com.safedriving.entity.enums.TripStatus;

import java.util.List;

public interface TripService {

    List<TripResponse> getAllTrips(TripStatus status, String driverId, String vehicleId);

    TripResponse getTripById(String id);

    TripResponse createTrip(TripRequest request);

    TripResponse updateTrip(String id, TripRequest request);

    void deleteTrip(String id);

    TripResponse startTrip(String id);

    TripResponse endTrip(String id, Double finalDistanceKm);

    TripResponse getActiveTripByDriver(String driverId);

    TripGpsTrackResponse recordGpsTrack(String tripId, TripGpsTrackRequest request);

    List<TripGpsTrackResponse> getGpsTracks(String tripId);
}
