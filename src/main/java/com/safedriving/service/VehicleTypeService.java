package com.safedriving.service;

import com.safedriving.dto.request.VehicleTypeRequest;
import com.safedriving.dto.response.VehicleTypeResponse;

import java.util.List;

public interface VehicleTypeService {

    List<VehicleTypeResponse> getAll();
    VehicleTypeResponse getById(Integer id);
    VehicleTypeResponse create(VehicleTypeRequest request);
    VehicleTypeResponse update(Integer id, VehicleTypeRequest request);
    void delete(Integer id);
}
