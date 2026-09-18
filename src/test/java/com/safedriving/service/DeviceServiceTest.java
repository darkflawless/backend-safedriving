package com.safedriving.service;

import com.safedriving.dto.request.DeviceRequest;
import com.safedriving.dto.response.DeviceResponse;
import com.safedriving.entity.Device;
import com.safedriving.entity.enums.DeviceStatus;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.DeviceRepository;
import com.safedriving.service.impl.DeviceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private DeviceServiceImpl deviceService;

    private Device testDevice;

    @BeforeEach
    void setUp() {
        testDevice = Device.builder()
                .id("device-1")
                .serialNumber("SN123")
                .macAddress("MAC123")
                .deviceStatus(DeviceStatus.ACTIVE)
                .startTime(LocalDateTime.now())
                .isDeleted(false)
                .build();
    }

    @Test
    @DisplayName("getAllDevices - Success")
    void getAllDevices_Success() {
        when(deviceRepository.findByIsDeletedFalse()).thenReturn(List.of(testDevice));

        List<DeviceResponse> responses = deviceService.getAllDevices();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("SN123", responses.get(0).getSerialNumber());
    }

    @Test
    @DisplayName("getDeviceById - Success")
    void getDeviceById_Success() {
        when(deviceRepository.findByIdAndIsDeletedFalse("device-1")).thenReturn(Optional.of(testDevice));

        DeviceResponse response = deviceService.getDeviceById("device-1");

        assertNotNull(response);
        assertEquals("SN123", response.getSerialNumber());
    }

    @Test
    @DisplayName("getDeviceById - NotFound")
    void getDeviceById_NotFound() {
        when(deviceRepository.findByIdAndIsDeletedFalse("device-unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> deviceService.getDeviceById("device-unknown"));
    }

    @Test
    @DisplayName("createDevice - Success")
    void createDevice_Success() {
        DeviceRequest request = DeviceRequest.builder()
                .serialNumber("SN999")
                .macAddress("MAC999")
                .deviceStatus(DeviceStatus.ACTIVE)
                .build();

        when(deviceRepository.existsBySerialNumberAndIsDeletedFalse("SN999")).thenReturn(false);
        when(deviceRepository.existsByMacAddressAndIsDeletedFalse("MAC999")).thenReturn(false);
        when(deviceRepository.save(any(Device.class))).thenAnswer(i -> {
            Device d = i.getArgument(0);
            d.setId("device-new");
            return d;
        });

        DeviceResponse response = deviceService.createDevice(request);

        assertNotNull(response);
        assertEquals("device-new", response.getId());
        assertEquals("SN999", response.getSerialNumber());
    }

    @Test
    @DisplayName("createDevice - Duplicate Serial")
    void createDevice_DuplicateSerial() {
        DeviceRequest request = DeviceRequest.builder()
                .serialNumber("SN123")
                .macAddress("MAC999")
                .build();

        when(deviceRepository.existsBySerialNumberAndIsDeletedFalse("SN123")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> deviceService.createDevice(request));
    }

    @Test
    @DisplayName("updateDevice - Success")
    void updateDevice_Success() {
        DeviceRequest request = DeviceRequest.builder()
                .deviceStatus(DeviceStatus.STOPPED)
                .build();

        when(deviceRepository.findByIdAndIsDeletedFalse("device-1")).thenReturn(Optional.of(testDevice));
        when(deviceRepository.save(any(Device.class))).thenReturn(testDevice);

        DeviceResponse response = deviceService.updateDevice("device-1", request);

        assertNotNull(response);
        assertEquals(DeviceStatus.STOPPED, response.getDeviceStatus());
    }

    @Test
    @DisplayName("deleteDevice - Success")
    void deleteDevice_Success() {
        when(deviceRepository.findByIdAndIsDeletedFalse("device-1")).thenReturn(Optional.of(testDevice));

        deviceService.deleteDevice("device-1");

        assertTrue(testDevice.getIsDeleted());
        verify(deviceRepository).save(testDevice);
    }
}
