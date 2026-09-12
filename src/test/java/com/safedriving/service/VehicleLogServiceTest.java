package com.safedriving.service;

import com.safedriving.dto.request.VehicleLogRequest;
import com.safedriving.dto.response.VehicleLogResponse;
import com.safedriving.entity.Vehicle;
import com.safedriving.entity.VehicleLog;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.VehicleLogRepository;
import com.safedriving.repository.VehicleRepository;
import com.safedriving.service.impl.VehicleLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleLogServiceTest {

    @Mock
    private VehicleLogRepository vehicleLogRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleLogServiceImpl vehicleLogService;

    private Vehicle testVehicle;
    private VehicleLog testLog;

    @BeforeEach
    void setUp() {
        testVehicle = Vehicle.builder()
                .id("veh-uuid-1")
                .plateNumber("29A-123.45")
                .isDeleted(false)
                .build();

        testLog = VehicleLog.builder()
                .id(100L)
                .vehicle(testVehicle)
                .timeVehicleLog(LocalDateTime.now())
                .lat(new BigDecimal("21.028511"))
                .lng(new BigDecimal("105.854444"))
                .build();
    }

    @Test
    @DisplayName("getAllVehicleLogs - Lấy tất cả khi vehicleId null")
    void getAllVehicleLogs_All_Success() {
        when(vehicleLogRepository.findAllByOrderByTimeVehicleLogDesc()).thenReturn(List.of(testLog));

        List<VehicleLogResponse> results = vehicleLogService.getAllVehicleLogs(null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(100L, results.get(0).getId());
        assertEquals("29A-123.45", results.get(0).getPlateNumber());
    }

    @Test
    @DisplayName("getAllVehicleLogs - Lọc theo vehicleId khi truyền vehicleId")
    void getAllVehicleLogs_ByVehicleId_Success() {
        when(vehicleLogRepository.findByVehicleIdOrderByTimeVehicleLogDesc("veh-uuid-1")).thenReturn(List.of(testLog));

        List<VehicleLogResponse> results = vehicleLogService.getAllVehicleLogs("veh-uuid-1");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("veh-uuid-1", results.get(0).getVehicleId());
    }

    @Test
    @DisplayName("getVehicleLogById - Thành công khi ID tồn tại")
    void getVehicleLogById_Success() {
        when(vehicleLogRepository.findById(100L)).thenReturn(Optional.of(testLog));

        VehicleLogResponse response = vehicleLogService.getVehicleLogById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("29A-123.45", response.getPlateNumber());
    }

    @Test
    @DisplayName("getVehicleLogById - Thất bại ném ResourceNotFoundException khi không tìm thấy")
    void getVehicleLogById_NotFound() {
        when(vehicleLogRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehicleLogService.getVehicleLogById(999L));
    }

    @Test
    @DisplayName("createVehicleLog - Thành công lưu log mới")
    void createVehicleLog_Success() {
        VehicleLogRequest request = VehicleLogRequest.builder()
                .vehicleId("veh-uuid-1")
                .timeVehicleLog(LocalDateTime.now())
                .lat(new BigDecimal("21.03"))
                .lng(new BigDecimal("105.86"))
                .build();

        when(vehicleRepository.findByIdAndIsDeletedFalse("veh-uuid-1")).thenReturn(Optional.of(testVehicle));
        when(vehicleLogRepository.save(any(VehicleLog.class))).thenAnswer(invocation -> {
            VehicleLog vl = invocation.getArgument(0);
            vl.setId(101L);
            return vl;
        });

        VehicleLogResponse response = vehicleLogService.createVehicleLog(request);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("29A-123.45", response.getPlateNumber());
        verify(vehicleLogRepository).save(any(VehicleLog.class));
    }

    @Test
    @DisplayName("createVehicleLog - Thất bại khi vehicleId không tồn tại")
    void createVehicleLog_VehicleNotFound() {
        VehicleLogRequest request = VehicleLogRequest.builder()
                .vehicleId("veh-non-existent")
                .build();

        when(vehicleRepository.findByIdAndIsDeletedFalse("veh-non-existent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehicleLogService.createVehicleLog(request));
    }

    @Test
    @DisplayName("deleteVehicleLog - Thành công xóa log")
    void deleteVehicleLog_Success() {
        when(vehicleLogRepository.findById(100L)).thenReturn(Optional.of(testLog));

        vehicleLogService.deleteVehicleLog(100L);

        verify(vehicleLogRepository).delete(testLog);
    }
}
