package com.safedriving.service;

import com.safedriving.dto.request.StopRequest;
import com.safedriving.dto.response.StopResponse;
import com.safedriving.entity.Address;
import com.safedriving.entity.Stop;
import com.safedriving.entity.enums.StopType;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.AddressRepository;
import com.safedriving.repository.StopRepository;
import com.safedriving.service.impl.StopServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StopServiceTest {

    @Mock
    private StopRepository stopRepository;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private StopServiceImpl stopService;

    private Address testAddress;
    private Stop testStop;

    @BeforeEach
    void setUp() {
        testAddress = Address.builder()
                .id("addr-1")
                .exactAddress("Bến xe Mỹ Đình")
                .commune("Mỹ Đình")
                .province("Hà Nội")
                .lat(new BigDecimal("21.028"))
                .lng(new BigDecimal("105.778"))
                .build();

        testStop = Stop.builder()
                .id("stop-1")
                .nameStop("Trạm Mỹ Đình")
                .address(testAddress)
                .type(StopType.STATION)
                .build();
    }

    @Test
    @DisplayName("getAllStops - Trả về danh sách điểm dừng thành công")
    void getAllStops_Success() {
        when(stopRepository.findAll()).thenReturn(List.of(testStop));

        List<StopResponse> results = stopService.getAllStops();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Trạm Mỹ Đình", results.get(0).getNameStop());
        assertEquals(StopType.STATION, results.get(0).getType());
        assertNotNull(results.get(0).getAddress());
        assertEquals("addr-1", results.get(0).getAddress().getId());
    }

    @Test
    @DisplayName("getStopById - Thành công khi ID tồn tại")
    void getStopById_Success() {
        when(stopRepository.findById("stop-1")).thenReturn(Optional.of(testStop));

        StopResponse response = stopService.getStopById("stop-1");

        assertNotNull(response);
        assertEquals("stop-1", response.getId());
        assertEquals("Trạm Mỹ Đình", response.getNameStop());
    }

    @Test
    @DisplayName("getStopById - Ném ResourceNotFoundException khi ID không tồn tại")
    void getStopById_NotFound() {
        when(stopRepository.findById("stop-non-existent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> stopService.getStopById("stop-non-existent"));
    }

    @Test
    @DisplayName("createStop - Thành công tạo điểm dừng")
    void createStop_Success() {
        StopRequest request = StopRequest.builder()
                .nameStop("Trạm Giáp Bát")
                .addressId("addr-1")
                .type(StopType.TERMINAL)
                .build();

        when(addressRepository.findById("addr-1")).thenReturn(Optional.of(testAddress));
        when(stopRepository.save(any(Stop.class))).thenAnswer(invocation -> {
            Stop s = invocation.getArgument(0);
            s.setId("stop-2");
            return s;
        });

        StopResponse response = stopService.createStop(request);

        assertNotNull(response);
        assertEquals("stop-2", response.getId());
        assertEquals("Trạm Giáp Bát", response.getNameStop());
        assertEquals(StopType.TERMINAL, response.getType());
        verify(stopRepository).save(any(Stop.class));
    }

    @Test
    @DisplayName("createStop - Ném ResourceNotFoundException khi addressId không tồn tại")
    void createStop_AddressNotFound() {
        StopRequest request = StopRequest.builder()
                .nameStop("Trạm Giáp Bát")
                .addressId("addr-unknown")
                .type(StopType.TERMINAL)
                .build();

        when(addressRepository.findById("addr-unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> stopService.createStop(request));
    }

    @Test
    @DisplayName("updateStop - Thành công cập nhật điểm dừng")
    void updateStop_Success() {
        StopRequest request = StopRequest.builder()
                .nameStop("Trạm Mỹ Đình Đổi Tên")
                .addressId("addr-1")
                .type(StopType.DEPOT)
                .build();

        when(stopRepository.findById("stop-1")).thenReturn(Optional.of(testStop));
        when(stopRepository.save(any(Stop.class))).thenReturn(testStop);

        StopResponse response = stopService.updateStop("stop-1", request);

        assertNotNull(response);
        assertEquals("Trạm Mỹ Đình Đổi Tên", response.getNameStop());
        assertEquals(StopType.DEPOT, response.getType());
        verify(stopRepository).save(testStop);
    }

    @Test
    @DisplayName("deleteStop - Thành công xóa điểm dừng")
    void deleteStop_Success() {
        when(stopRepository.findById("stop-1")).thenReturn(Optional.of(testStop));

        stopService.deleteStop("stop-1");

        verify(stopRepository).delete(testStop);
    }
}
