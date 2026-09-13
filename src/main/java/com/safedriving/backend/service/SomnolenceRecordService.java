package com.safedriving.backend.service;

import com.safedriving.backend.dto.request.SomnolenceRecordRequest;
import com.safedriving.backend.dto.response.SomnolenceRecordResponse;

import java.util.List;

public interface SomnolenceRecordService {
    List<SomnolenceRecordResponse> getAll();
    SomnolenceRecordResponse getById(Long id);
    List<SomnolenceRecordResponse> getByDriverId(String driverId);
    SomnolenceRecordResponse create(SomnolenceRecordRequest request);
    void delete(Long id);
}
