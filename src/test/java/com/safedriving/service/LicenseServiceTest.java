package com.safedriving.service;

import com.safedriving.dto.request.LicenseClassReference;
import com.safedriving.dto.request.LicenseRequest;
import com.safedriving.dto.response.LicenseResponse;
import com.safedriving.entity.Driver;
import com.safedriving.entity.License;
import com.safedriving.entity.LicenseClass;
import com.safedriving.entity.Staff;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.DriverRepository;
import com.safedriving.repository.LicenseClassRepository;
import com.safedriving.repository.LicenseRepository;
import com.safedriving.service.impl.LicenseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LicenseServiceTest {

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private LicenseClassRepository licenseClassRepository;

    @InjectMocks
    private LicenseServiceImpl licenseService;

    private License testLicense;
    private Driver testDriver;
    private LicenseClass testLicenseClass;

    @BeforeEach
    void setUp() {
        Staff staff = Staff.builder().id("staff-1").firstName("Van A").lastName("Nguyen").build();
        testDriver = Driver.builder().id("driver-1").staff(staff).build();
        testLicenseClass = LicenseClass.builder().id((short) 1).code("B2").name("Hạng B2").capacity(9).build();

        testLicense = License.builder()
                .id("license-1")
                .licenseNo("123456789012")
                .issueDate(LocalDate.of(2020, 1, 1))
                .licenseExpiry(LocalDate.of(2030, 1, 1))
                .placeOfIssue("Hà Nội")
                .driver(testDriver)
                .licenseClass(testLicenseClass)
                .isDeleted(false)
                .build();
    }

    @Test
    @DisplayName("getAllLicenses - Success without driverId")
    void getAllLicenses_Success_NoDriverId() {
        when(licenseRepository.findByIsDeletedFalse()).thenReturn(List.of(testLicense));

        List<LicenseResponse> responses = licenseService.getAllLicenses(null);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("123456789012", responses.get(0).getLicenseNo());
        assertEquals("Van A Nguyen", responses.get(0).getDriverName());
    }

    @Test
    @DisplayName("getAllLicenses - Success with driverId")
    void getAllLicenses_Success_WithDriverId() {
        when(licenseRepository.findByDriverIdAndIsDeletedFalse("driver-1")).thenReturn(List.of(testLicense));

        List<LicenseResponse> responses = licenseService.getAllLicenses("driver-1");

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    @DisplayName("getLicenseById - Success")
    void getLicenseById_Success() {
        when(licenseRepository.findByIdAndIsDeletedFalse("license-1")).thenReturn(Optional.of(testLicense));

        LicenseResponse response = licenseService.getLicenseById("license-1");

        assertNotNull(response);
        assertEquals("123456789012", response.getLicenseNo());
    }

    @Test
    @DisplayName("getLicenseById - NotFound")
    void getLicenseById_NotFound() {
        when(licenseRepository.findByIdAndIsDeletedFalse("license-999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> licenseService.getLicenseById("license-999"));
    }

    @Test
    @DisplayName("createLicense - Success")
    void createLicense_Success() {
        LicenseRequest request = LicenseRequest.builder()
                .licenseNo("987654321098")
                .issueDate(LocalDate.of(2021, 5, 1))
                .licenseExpiry(LocalDate.of(2031, 5, 1))
                .placeOfIssue("TP.HCM")
                .driverId("driver-1")
                .licenseClass(LicenseClassReference.builder().id(1).build())
                .build();

        when(driverRepository.findByIdAndIsDeletedFalse("driver-1")).thenReturn(Optional.of(testDriver));
        when(licenseClassRepository.findById((short) 1)).thenReturn(Optional.of(testLicenseClass));
        when(licenseRepository.existsByLicenseNoAndIsDeletedFalse("987654321098")).thenReturn(false);
        when(licenseRepository.save(any(License.class))).thenAnswer(i -> {
            License l = i.getArgument(0);
            l.setId("license-2");
            return l;
        });

        LicenseResponse response = licenseService.createLicense(request);

        assertNotNull(response);
        assertEquals("license-2", response.getId());
        assertEquals("987654321098", response.getLicenseNo());
    }

    @Test
    @DisplayName("createLicense - Missing Driver")
    void createLicense_MissingDriver() {
        LicenseRequest request = LicenseRequest.builder()
                .licenseNo("987654321098")
                .licenseClass(LicenseClassReference.builder().id(1).build())
                .build();

        assertThrows(BadRequestException.class, () -> licenseService.createLicense(request));
    }

    @Test
    @DisplayName("updateLicense - Success")
    void updateLicense_Success() {
        LicenseRequest request = LicenseRequest.builder()
                .placeOfIssue("Đà Nẵng")
                .build();

        when(licenseRepository.findByIdAndIsDeletedFalse("license-1")).thenReturn(Optional.of(testLicense));
        when(licenseRepository.save(any(License.class))).thenReturn(testLicense);

        LicenseResponse response = licenseService.updateLicense("license-1", request);

        assertNotNull(response);
        assertEquals("Đà Nẵng", response.getPlaceOfIssue());
    }

    @Test
    @DisplayName("deleteLicense - Success")
    void deleteLicense_Success() {
        when(licenseRepository.findByIdAndIsDeletedFalse("license-1")).thenReturn(Optional.of(testLicense));

        licenseService.deleteLicense("license-1");

        assertTrue(testLicense.getIsDeleted());
        verify(licenseRepository).save(testLicense);
    }
}
