package com.safedriving.service;

import com.safedriving.dto.request.AssignDriverRequest;
import com.safedriving.dto.request.VehicleRequest;
import com.safedriving.dto.response.VehicleResponse;
import com.safedriving.entity.enums.VehicleStatus;

import java.util.List;

/**
 * Service interface quản lý phương tiện và phân công tài xế.
 *
 * @author phanthuy0811
 */
public interface VehicleService {

    List<VehicleResponse> getAllVehicles(VehicleStatus status);

    VehicleResponse getVehicleById(String id);

    VehicleResponse createVehicle(VehicleRequest request);

    VehicleResponse updateVehicle(String id, VehicleRequest request);

    void deleteVehicle(String id);

    VehicleResponse assignDriver(String vehicleId, AssignDriverRequest request);
}
