package com.safedriving.service;

import com.safedriving.dto.request.AlcoholRecordRequest;
import com.safedriving.dto.response.AlcoholRecordResponse;

import java.util.List;

public interface AlcoholRecordService {

    List<AlcoholRecordResponse> getAllAlcoholRecords(String driverId);

    AlcoholRecordResponse getAlcoholRecordById(Long id);

    AlcoholRecordResponse createAlcoholRecord(AlcoholRecordRequest request);

    AlcoholRecordResponse updateAlcoholRecord(Long id, AlcoholRecordRequest request);

    void deleteAlcoholRecord(Long id);
}
