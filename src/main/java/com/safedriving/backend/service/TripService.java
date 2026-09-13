package com.safedriving.backend.service;

import com.safedriving.backend.dto.request.TripRequest;
import com.safedriving.backend.dto.response.TripResponse;

import java.util.List;

public interface TripService {
    List<TripResponse> getAll();
    TripResponse getById(String id);
    TripResponse create(TripRequest request);
    TripResponse update(String id, TripRequest request);
    void delete(String id);
}
