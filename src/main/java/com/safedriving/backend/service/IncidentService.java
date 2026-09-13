package com.safedriving.backend.service;

import com.safedriving.backend.dto.request.IncidentRequest;
import com.safedriving.backend.dto.response.IncidentResponse;

import java.util.List;

public interface IncidentService {
    List<IncidentResponse> getAll();
    IncidentResponse getById(String id);
    List<IncidentResponse> getByTripId(String tripId);
    IncidentResponse create(IncidentRequest request);
    IncidentResponse update(String id, IncidentRequest request);
    void delete(String id);
}
