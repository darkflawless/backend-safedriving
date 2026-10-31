package com.safedriving.service;

import com.safedriving.dto.request.StopRequest;
import com.safedriving.dto.response.StopResponse;

import java.util.List;

public interface StopService {

    List<StopResponse> getAllStops();

    StopResponse getStopById(String id);

    StopResponse createStop(StopRequest request);

    StopResponse updateStop(String id, StopRequest request);

    void deleteStop(String id);
}
