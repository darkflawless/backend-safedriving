package com.safedriving.backend.service;

import com.safedriving.backend.dto.request.AssignmentRequest;
import com.safedriving.backend.dto.response.AssignmentResponse;

import java.util.List;

public interface AssignmentService {
    List<AssignmentResponse> getAll();
    AssignmentResponse getById(String id);
    List<AssignmentResponse> getByDriverId(String driverId);
    AssignmentResponse create(AssignmentRequest request);
    AssignmentResponse update(String id, AssignmentRequest request);
    void delete(String id);
}
