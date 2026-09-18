package com.safedriving.service;

import com.safedriving.dto.request.LicenseRequest;
import com.safedriving.dto.response.LicenseResponse;

import java.util.List;

public interface LicenseService {

    List<LicenseResponse> getAllLicenses(String driverId);

    LicenseResponse getLicenseById(String id);

    LicenseResponse createLicense(LicenseRequest request);

    LicenseResponse updateLicense(String id, LicenseRequest request);

    void deleteLicense(String id);
}
