package com.safedriving.service;

import com.safedriving.dto.request.DeviceRequest;
import com.safedriving.dto.response.DeviceResponse;

import java.util.List;

public interface DeviceService {

    List<DeviceResponse> getAllDevices();

    DeviceResponse getDeviceById(String id);

    DeviceResponse createDevice(DeviceRequest request);

    DeviceResponse updateDevice(String id, DeviceRequest request);

    void deleteDevice(String id);
}
