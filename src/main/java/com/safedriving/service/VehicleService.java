package com.safedriving.service;

import com.safedriving.dto.request.VehicleRequest;
import com.safedriving.dto.response.VehicleResponse;
import java.util.List;
public interface VehicleService {
    List<VehicleResponse> getAll();
    VehicleResponse getById(String id);
    VehicleResponse create(VehicleRequest request);
    VehicleResponse update(String id, VehicleRequest request);
    void delete(String id);
}
