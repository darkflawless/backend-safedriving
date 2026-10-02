package com.safedriving.service;

import com.safedriving.dto.request.StaffRequest;
import com.safedriving.dto.response.StaffResponse;

import java.util.List;

public interface StaffService {

    List<StaffResponse> getAllStaff();

    StaffResponse getStaffById(String id);

    StaffResponse createStaff(StaffRequest request);

    StaffResponse updateStaff(String id, StaffRequest request);

    void deleteStaff(String id);
}
