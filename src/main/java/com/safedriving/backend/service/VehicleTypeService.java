package com.safedriving.backend.service;

import com.safedriving.backend.dto.request.VehicleTypeRequest;
import com.safedriving.backend.dto.response.VehicleTypeResponse;

import java.util.List;

public interface VehicleTypeService {

    List<VehicleTypeResponse> getAll();
    VehicleTypeResponse getById(Integer id);
    VehicleTypeResponse create(VehicleTypeRequest request);
    VehicleTypeResponse update(Integer id, VehicleTypeRequest request);
    void delete(Integer id);
}
