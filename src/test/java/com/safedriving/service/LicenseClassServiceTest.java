package com.safedriving.service;

import com.safedriving.dto.request.LicenseClassRequest;
import com.safedriving.dto.response.LicenseClassResponse;
import com.safedriving.entity.LicenseClass;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.LicenseClassRepository;
import com.safedriving.service.impl.LicenseClassServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LicenseClassServiceTest {

    @Mock
    private LicenseClassRepository licenseClassRepository;

    @InjectMocks
    private LicenseClassServiceImpl licenseClassService;

    private LicenseClass testLicenseClass;

    @BeforeEach
    void setUp() {
        testLicenseClass = LicenseClass.builder()
                .id((short) 1)
                .code("B2")
                .name("Hạng B2")
                .capacity(9)
                .build();
    }

    @Test
    @DisplayName("getAllLicenseClasses - Success")
    void getAllLicenseClasses_Success() {
        when(licenseClassRepository.findAll()).thenReturn(List.of(testLicenseClass));

        List<LicenseClassResponse> responses = licenseClassService.getAllLicenseClasses();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("B2", responses.get(0).getCode());
    }

    @Test
    @DisplayName("getLicenseClassById - Success")
    void getLicenseClassById_Success() {
        when(licenseClassRepository.findById((short) 1)).thenReturn(Optional.of(testLicenseClass));

        LicenseClassResponse response = licenseClassService.getLicenseClassById((short) 1);

        assertNotNull(response);
        assertEquals("B2", response.getCode());
    }

    @Test
    @DisplayName("getLicenseClassById - NotFound")
    void getLicenseClassById_NotFound() {
        when(licenseClassRepository.findById((short) 99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> licenseClassService.getLicenseClassById((short) 99));
    }

    @Test
    @DisplayName("createLicenseClass - Success")
    void createLicenseClass_Success() {
        LicenseClassRequest request = LicenseClassRequest.builder()
                .id((short) 2)
                .code("C")
                .name("Hạng C")
                .capacity(30)
                .build();

        when(licenseClassRepository.existsById((short) 2)).thenReturn(false);
        when(licenseClassRepository.existsByCode("C")).thenReturn(false);
        when(licenseClassRepository.save(any(LicenseClass.class))).thenAnswer(i -> i.getArgument(0));

        LicenseClassResponse response = licenseClassService.createLicenseClass(request);

        assertNotNull(response);
        assertEquals("C", response.getCode());
    }

    @Test
    @DisplayName("createLicenseClass - IdExists")
    void createLicenseClass_IdExists() {
        LicenseClassRequest request = LicenseClassRequest.builder()
                .id((short) 1)
                .code("C")
                .build();

        when(licenseClassRepository.existsById((short) 1)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> licenseClassService.createLicenseClass(request));
    }

    @Test
    @DisplayName("updateLicenseClass - Success")
    void updateLicenseClass_Success() {
        LicenseClassRequest request = LicenseClassRequest.builder()
                .code("B2")
                .capacity(10)
                .build();

        when(licenseClassRepository.findById((short) 1)).thenReturn(Optional.of(testLicenseClass));
        when(licenseClassRepository.save(any(LicenseClass.class))).thenReturn(testLicenseClass);

        LicenseClassResponse response = licenseClassService.updateLicenseClass((short) 1, request);

        assertNotNull(response);
        assertEquals(10, response.getCapacity());
    }

    @Test
    @DisplayName("deleteLicenseClass - Success")
    void deleteLicenseClass_Success() {
        when(licenseClassRepository.findById((short) 1)).thenReturn(Optional.of(testLicenseClass));

        licenseClassService.deleteLicenseClass((short) 1);

        verify(licenseClassRepository).delete(testLicenseClass);
    }
}
