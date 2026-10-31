package com.safedriving.service;

import com.safedriving.dto.request.AssignmentRequest;
import com.safedriving.dto.response.AssignmentResponse;

import com.safedriving.security.CustomUserDetails;

import java.time.LocalDate;
import java.util.List;

public interface AssignmentService {
    List<AssignmentResponse> getAll();
    AssignmentResponse getById(String id);
    List<AssignmentResponse> getByDriverId(String driverId);
    List<AssignmentResponse> getTodayByDriverId(String driverId, LocalDate date);
    List<AssignmentResponse> getTodayAssignmentsForCurrentDriver(CustomUserDetails userDetails, LocalDate date);
    AssignmentResponse create(AssignmentRequest request);
    AssignmentResponse update(String id, AssignmentRequest request);
    void delete(String id);
}
