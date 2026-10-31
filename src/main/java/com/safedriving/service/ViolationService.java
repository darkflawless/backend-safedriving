package com.safedriving.service;

import com.safedriving.dto.request.ViolationRequest;
import com.safedriving.dto.response.ViolationResponse;

import java.util.List;

public interface ViolationService {

    List<ViolationResponse> getAllViolations(String driverId);

    ViolationResponse getViolationById(String id);

    ViolationResponse createViolation(ViolationRequest request);

    ViolationResponse updateViolation(String id, ViolationRequest request);

    void deleteViolation(String id);
}
