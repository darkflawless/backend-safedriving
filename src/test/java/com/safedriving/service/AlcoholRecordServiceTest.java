package com.safedriving.service;

import com.safedriving.dto.request.AlcoholRecordRequest;
import com.safedriving.dto.response.AlcoholRecordResponse;
import com.safedriving.entity.AlcoholRecord;
import com.safedriving.entity.Driver;
import com.safedriving.entity.Staff;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.AlcoholRecordRepository;
import com.safedriving.repository.DriverRepository;
import com.safedriving.service.impl.AlcoholRecordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

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
class AlcoholRecordServiceTest {

    @Mock
    private AlcoholRecordRepository alcoholRecordRepository;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private AlcoholRecordServiceImpl alcoholRecordService;

    private AlcoholRecord testRecord;
    private Driver testDriver;

    @BeforeEach
    void setUp() {
        Staff staff = Staff.builder().id("staff-1").firstName("Tran").lastName("A").build();
        testDriver = Driver.builder().id("driver-1").staff(staff).build();

        testRecord = AlcoholRecord.builder()
                .id(1L)
                .driver(testDriver)
                .alcoholLevel(new BigDecimal("0.0"))
                .measurementTime(LocalDateTime.now())
                .notes("OK")
                .build();
    }

    @Test
    @DisplayName("getAllAlcoholRecords - Success without driverId")
    void getAllAlcoholRecords_Success_NoDriverId() {
        when(alcoholRecordRepository.findAll(any(Sort.class))).thenReturn(List.of(testRecord));

        List<AlcoholRecordResponse> responses = alcoholRecordService.getAllAlcoholRecords(null);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(new BigDecimal("0.0"), responses.get(0).getAlcoholLevel());
    }

    @Test
    @DisplayName("getAllAlcoholRecords - Success with driverId")
    void getAllAlcoholRecords_Success_WithDriverId() {
        when(alcoholRecordRepository.findByDriverIdOrderByMeasurementTimeDesc("driver-1")).thenReturn(List.of(testRecord));

        List<AlcoholRecordResponse> responses = alcoholRecordService.getAllAlcoholRecords("driver-1");

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    @DisplayName("getAlcoholRecordById - Success")
    void getAlcoholRecordById_Success() {
        when(alcoholRecordRepository.findById(1L)).thenReturn(Optional.of(testRecord));

        AlcoholRecordResponse response = alcoholRecordService.getAlcoholRecordById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    @DisplayName("getAlcoholRecordById - NotFound")
    void getAlcoholRecordById_NotFound() {
        when(alcoholRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> alcoholRecordService.getAlcoholRecordById(99L));
    }

    @Test
    @DisplayName("createAlcoholRecord - Success")
    void createAlcoholRecord_Success() {
        AlcoholRecordRequest request = AlcoholRecordRequest.builder()
                .driverId("driver-1")
                .alcoholLevel(new BigDecimal("0.5"))
                .build();

        when(driverRepository.findByIdAndIsDeletedFalse("driver-1")).thenReturn(Optional.of(testDriver));
        when(alcoholRecordRepository.save(any(AlcoholRecord.class))).thenAnswer(i -> {
            AlcoholRecord r = i.getArgument(0);
            r.setId(2L);
            return r;
        });

        AlcoholRecordResponse response = alcoholRecordService.createAlcoholRecord(request);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals(new BigDecimal("0.5"), response.getAlcoholLevel());
    }

    @Test
    @DisplayName("createAlcoholRecord - Missing Driver")
    void createAlcoholRecord_MissingDriver() {
        AlcoholRecordRequest request = AlcoholRecordRequest.builder()
                .alcoholLevel(new BigDecimal("0.5"))
                .build();

        assertThrows(BadRequestException.class, () -> alcoholRecordService.createAlcoholRecord(request));
    }

    @Test
    @DisplayName("updateAlcoholRecord - Success")
    void updateAlcoholRecord_Success() {
        AlcoholRecordRequest request = AlcoholRecordRequest.builder()
                .alcoholLevel(new BigDecimal("0.2"))
                .build();

        when(alcoholRecordRepository.findById(1L)).thenReturn(Optional.of(testRecord));
        when(alcoholRecordRepository.save(any(AlcoholRecord.class))).thenReturn(testRecord);

        AlcoholRecordResponse response = alcoholRecordService.updateAlcoholRecord(1L, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("0.2"), response.getAlcoholLevel());
    }

    @Test
    @DisplayName("deleteAlcoholRecord - Success")
    void deleteAlcoholRecord_Success() {
        when(alcoholRecordRepository.findById(1L)).thenReturn(Optional.of(testRecord));

        alcoholRecordService.deleteAlcoholRecord(1L);

        verify(alcoholRecordRepository).delete(testRecord);
    }
}
