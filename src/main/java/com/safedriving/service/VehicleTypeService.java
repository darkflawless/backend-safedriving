package com.safedriving.service;

import com.safedriving.dto.request.VehicleTypeRequest;
import com.safedriving.dto.response.VehicleTypeResponse;

import java.util.List;

/**
 * Service interface quản lý loại phương tiện.
 *
 * @author phanthuy0811
 */
public interface VehicleTypeService {

    List<VehicleTypeResponse> getAll();

    VehicleTypeResponse getById(Short id);

    VehicleTypeResponse create(VehicleTypeRequest request);

    VehicleTypeResponse update(Short id, VehicleTypeRequest request);

    void delete(Short id);
}
