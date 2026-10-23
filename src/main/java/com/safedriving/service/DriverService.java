package com.safedriving.service;

import com.safedriving.dto.request.DriverCreateRequest;
import com.safedriving.dto.request.DriverRequest;
import com.safedriving.dto.response.DriverResponse;

import java.util.List;

public interface DriverService {

    List<DriverResponse> getAllDrivers();

    DriverResponse getDriverById(String id);

    DriverResponse createDriver(DriverRequest request);

    DriverResponse createDriver(DriverCreateRequest request, org.springframework.web.multipart.MultipartFile avatar);

    DriverResponse updateDriver(String id, DriverRequest request);

    void deleteDriver(String id);
}
