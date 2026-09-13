package com.safedriving.backend.service;

import com.safedriving.backend.dto.request.VehicleRequest;
import com.safedriving.backend.dto.response.VehicleResponse;
import java.util.List;
public interface VehicleService {
    List<VehicleResponse> getAll();
    VehicleResponse getById(String id);
    VehicleResponse create(VehicleRequest request);
    VehicleResponse update(String id, VehicleRequest request);
    void delete(String id);
}
