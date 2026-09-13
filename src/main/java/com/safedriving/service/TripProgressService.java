package com.safedriving.service;

import com.safedriving.dto.request.TripProgressRequest;
import com.safedriving.dto.response.TripProgressResponse;

import java.util.List;

public interface TripProgressService {

    List<TripProgressResponse> getAllTripProgress(String tripId);

    TripProgressResponse getTripProgressById(String id);

    TripProgressResponse createTripProgress(TripProgressRequest request);

    TripProgressResponse updateTripProgress(String id, TripProgressRequest request);

    void deleteTripProgress(String id);
}
