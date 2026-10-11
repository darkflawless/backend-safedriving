package com.safedriving.service;

import com.safedriving.dto.request.TripRequest;
import com.safedriving.dto.response.TripResponse;

import java.util.List;

public interface TripService {
    List<TripResponse> getAll();
    TripResponse getById(String id);
    TripResponse create(TripRequest request);
    TripResponse update(String id, TripRequest request);
    void delete(String id);
}
