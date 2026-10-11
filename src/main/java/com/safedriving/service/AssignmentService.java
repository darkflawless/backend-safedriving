package com.safedriving.service;

import com.safedriving.dto.request.AssignmentRequest;
import com.safedriving.dto.response.AssignmentResponse;

import java.util.List;

public interface AssignmentService {
    List<AssignmentResponse> getAll();
    AssignmentResponse getById(String id);
    List<AssignmentResponse> getByDriverId(String driverId);
    AssignmentResponse create(AssignmentRequest request);
    AssignmentResponse update(String id, AssignmentRequest request);
    void delete(String id);
}
