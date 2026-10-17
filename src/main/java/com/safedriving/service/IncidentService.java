package com.safedriving.service;

import com.safedriving.dto.request.IncidentRequest;
import com.safedriving.dto.response.IncidentResponse;

import java.util.List;

public interface IncidentService {
    List<IncidentResponse> getAll();
    IncidentResponse getById(String id);
    List<IncidentResponse> getByTripId(String tripId);
    IncidentResponse create(IncidentRequest request);
    IncidentResponse update(String id, IncidentRequest request);
    void delete(String id);
}
