package com.safedriving.service;

import com.safedriving.dto.request.AssignDriverRequest;
import com.safedriving.dto.response.VehicleResponse;
import com.safedriving.entity.enums.VehicleStatus;

import java.util.List;

public interface VehicleService {

    List<VehicleResponse> getAllVehicles(VehicleStatus status);

    VehicleResponse getVehicleById(String id);

    VehicleResponse assignDriver(String vehicleId, AssignDriverRequest request);
}
