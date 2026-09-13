package com.safedriving.service;

import com.safedriving.dto.request.TripProgressRequest;
import com.safedriving.dto.response.TripProgressResponse;
import com.safedriving.entity.Trip;
import com.safedriving.entity.TripProgress;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.TripProgressRepository;
import com.safedriving.repository.TripRepository;
import com.safedriving.service.impl.TripProgressServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripProgressServiceTest {

    @Mock
    private TripProgressRepository tripProgressRepository;

    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private TripProgressServiceImpl tripProgressService;

    private Trip testTrip;
    private TripProgress testProgress;

    @BeforeEach
    void setUp() {
        testTrip = Trip.builder()
                .id("trip-1")
                .isDeleted(false)
                .build();

        testProgress = TripProgress.builder()
                .id("tp-1")
                .trip(testTrip)
                .order(1)
                .arrive(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getAllTripProgress - Lấy tất cả khi tripId null")
    void getAllTripProgress_All_Success() {
        when(tripProgressRepository.findAllByOrderByArriveDesc()).thenReturn(List.of(testProgress));

        List<TripProgressResponse> results = tripProgressService.getAllTripProgress(null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("tp-1", results.get(0).getId());
    }

    @Test
    @DisplayName("getAllTripProgress - Lọc theo tripId")
    void getAllTripProgress_ByTripId_Success() {
        when(tripProgressRepository.findByTripIdOrderByOrderAsc("trip-1")).thenReturn(List.of(testProgress));

        List<TripProgressResponse> results = tripProgressService.getAllTripProgress("trip-1");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("trip-1", results.get(0).getTripId());
    }

    @Test
    @DisplayName("getTripProgressById - Lấy chi tiết khi ID tồn tại")
    void getTripProgressById_Success() {
        when(tripProgressRepository.findById("tp-1")).thenReturn(Optional.of(testProgress));

        TripProgressResponse response = tripProgressService.getTripProgressById("tp-1");

        assertNotNull(response);
        assertEquals("tp-1", response.getId());
    }

    @Test
    @DisplayName("createTripProgress - Thành công lưu tiến độ chuyến đi")
    void createTripProgress_Success() {
        TripProgressRequest request = TripProgressRequest.builder()
                .tripId("trip-1")
                .order(1)
                .arrive(LocalDateTime.now())
                .build();

        when(tripRepository.findByIdAndIsDeletedFalse("trip-1")).thenReturn(Optional.of(testTrip));
        when(tripProgressRepository.save(any(TripProgress.class))).thenAnswer(invocation -> {
            TripProgress tp = invocation.getArgument(0);
            tp.setId("tp-2");
            return tp;
        });

        TripProgressResponse response = tripProgressService.createTripProgress(request);

        assertNotNull(response);
        assertEquals("tp-2", response.getId());
        assertEquals(1, response.getOrder());
        verify(tripProgressRepository).save(any(TripProgress.class));
    }

    @Test
    @DisplayName("deleteTripProgress - Xóa tiến độ thành công")
    void deleteTripProgress_Success() {
        when(tripProgressRepository.findById("tp-1")).thenReturn(Optional.of(testProgress));

        tripProgressService.deleteTripProgress("tp-1");

        verify(tripProgressRepository).delete(testProgress);
    }
}
