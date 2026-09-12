package com.safedriving.service;

import com.safedriving.dto.request.ViolationRequest;
import com.safedriving.dto.response.ViolationResponse;
import com.safedriving.entity.Account;
import com.safedriving.entity.Driver;
import com.safedriving.entity.Incident;
import com.safedriving.entity.Violation;
import com.safedriving.entity.enums.ViolationType;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.AccountRepository;
import com.safedriving.repository.DriverRepository;
import com.safedriving.repository.IncidentRepository;
import com.safedriving.repository.ViolationRepository;
import com.safedriving.service.impl.ViolationServiceImpl;
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
class ViolationServiceTest {

    @Mock
    private ViolationRepository violationRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private ViolationServiceImpl violationService;

    private Driver testDriver;
    private Incident testIncident;
    private Account testAccount;
    private Violation testViolation;

    @BeforeEach
    void setUp() {
        testDriver = Driver.builder()
                .id("driver-1")
                .isDeleted(false)
                .build();

        testIncident = Incident.builder()
                .id("incident-1")
                .build();

        testAccount = Account.builder()
                .id("acc-1")
                .username("officer1")
                .build();

        testViolation = Violation.builder()
                .id("vio-1")
                .driver(testDriver)
                .incident(testIncident)
                .account(testAccount)
                .timeViolation(LocalDateTime.now())
                .type(ViolationType.SPEEDING)
                .penalty(new BigDecimal("5000000"))
                .note("Chạy quá tốc độ 85/60 km/h")
                .build();
    }

    @Test
    @DisplayName("getAllViolations - Lấy tất cả khi driverId null")
    void getAllViolations_All_Success() {
        when(violationRepository.findAllByOrderByTimeViolationDesc()).thenReturn(List.of(testViolation));

        List<ViolationResponse> results = violationService.getAllViolations(null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("vio-1", results.get(0).getId());
        assertEquals(ViolationType.SPEEDING, results.get(0).getType());
    }

    @Test
    @DisplayName("getAllViolations - Lọc theo driverId")
    void getAllViolations_ByDriverId_Success() {
        when(violationRepository.findByDriverIdOrderByTimeViolationDesc("driver-1")).thenReturn(List.of(testViolation));

        List<ViolationResponse> results = violationService.getAllViolations("driver-1");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("driver-1", results.get(0).getDriverId());
    }

    @Test
    @DisplayName("getViolationById - Lấy chi tiết khi ID tồn tại")
    void getViolationById_Success() {
        when(violationRepository.findById("vio-1")).thenReturn(Optional.of(testViolation));

        ViolationResponse response = violationService.getViolationById("vio-1");

        assertNotNull(response);
        assertEquals("vio-1", response.getId());
        assertEquals(new BigDecimal("5000000"), response.getPenalty());
    }

    @Test
    @DisplayName("createViolation - Thành công lập biên bản vi phạm")
    void createViolation_Success() {
        ViolationRequest request = ViolationRequest.builder()
                .driverId("driver-1")
                .incidentId("incident-1")
                .accountId("acc-1")
                .type(ViolationType.SPEEDING)
                .penalty(new BigDecimal("2000000"))
                .note("Vi phạm làn đường")
                .build();

        when(driverRepository.findByIdAndIsDeletedFalse("driver-1")).thenReturn(Optional.of(testDriver));
        when(incidentRepository.findById("incident-1")).thenReturn(Optional.of(testIncident));
        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(testAccount));
        when(violationRepository.save(any(Violation.class))).thenAnswer(invocation -> {
            Violation v = invocation.getArgument(0);
            v.setId("vio-2");
            return v;
        });

        ViolationResponse response = violationService.createViolation(request);

        assertNotNull(response);
        assertEquals("vio-2", response.getId());
        assertEquals(ViolationType.SPEEDING, response.getType());
        verify(violationRepository).save(any(Violation.class));
    }

    @Test
    @DisplayName("createViolation - Ném ResourceNotFoundException khi driver không tồn tại")
    void createViolation_DriverNotFound() {
        ViolationRequest request = ViolationRequest.builder()
                .driverId("driver-unknown")
                .incidentId("incident-1")
                .accountId("acc-1")
                .type(ViolationType.SPEEDING)
                .build();

        when(driverRepository.findByIdAndIsDeletedFalse("driver-unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> violationService.createViolation(request));
    }

    @Test
    @DisplayName("deleteViolation - Xóa biên bản thành công")
    void deleteViolation_Success() {
        when(violationRepository.findById("vio-1")).thenReturn(Optional.of(testViolation));

        violationService.deleteViolation("vio-1");

        verify(violationRepository).delete(testViolation);
    }
}
