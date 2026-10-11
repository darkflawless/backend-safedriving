package com.safedriving.service;

import com.safedriving.dto.request.RouteStopRequest;
import com.safedriving.dto.response.RouteStopResponse;
import com.safedriving.entity.Address;
import com.safedriving.entity.Route;
import com.safedriving.entity.RouteStop;
import com.safedriving.entity.Stop;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.RouteRepository;
import com.safedriving.repository.RouteStopRepository;
import com.safedriving.repository.StopRepository;
import com.safedriving.service.impl.RouteStopServiceImpl;
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
class RouteStopServiceTest {

    @Mock
    private RouteStopRepository routeStopRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private StopRepository stopRepository;

    @InjectMocks
    private RouteStopServiceImpl routeStopService;

    private Route testRoute;
    private Stop testStop;
    private RouteStop testRouteStop;

    @BeforeEach
    void setUp() {
        testRoute = Route.builder()
                .id("route-1")
                .code("HN-HP")
                .routeName("Hà Nội - Hải Phòng")
                .build();

        testStop = Stop.builder()
                .id("stop-1")
                .nameStop("Bến xe Gia Lâm")
                .address(Address.builder().id("addr-1").exactAddress("Gia Lâm").build())
                .build();

        testRouteStop = RouteStop.builder()
                .id("rs-1")
                .route(testRoute)
                .stop(testStop)
                .order(1)
                .build();
    }

    @Test
    @DisplayName("getAllRouteStops - Lấy tất cả khi routeId null")
    void getAllRouteStops_All_Success() {
        when(routeStopRepository.findAllByOrderByRouteIdAscOrderAsc()).thenReturn(List.of(testRouteStop));

        List<RouteStopResponse> results = routeStopService.getAllRouteStops(null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("rs-1", results.get(0).getId());
        assertEquals("HN-HP", results.get(0).getRouteCode());
    }

    @Test
    @DisplayName("getAllRouteStops - Lọc theo routeId thành công")
    void getAllRouteStops_ByRouteId_Success() {
        when(routeStopRepository.findByRouteIdOrderByOrderAsc("route-1")).thenReturn(List.of(testRouteStop));

        List<RouteStopResponse> results = routeStopService.getAllRouteStops("route-1");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("route-1", results.get(0).getRouteId());
    }

    @Test
    @DisplayName("getRouteStopById - Lấy chi tiết khi ID tồn tại")
    void getRouteStopById_Success() {
        when(routeStopRepository.findById("rs-1")).thenReturn(Optional.of(testRouteStop));

        RouteStopResponse response = routeStopService.getRouteStopById("rs-1");

        assertNotNull(response);
        assertEquals("rs-1", response.getId());
        assertEquals(1, response.getOrder());
    }

    @Test
    @DisplayName("createRouteStop - Thành công liên kết điểm dừng vào tuyến")
    void createRouteStop_Success() {
        RouteStopRequest request = RouteStopRequest.builder()
                .routeId("route-1")
                .stopId("stop-1")
                .order(2)
                .build();

        when(routeRepository.findById("route-1")).thenReturn(Optional.of(testRoute));
        when(stopRepository.findById("stop-1")).thenReturn(Optional.of(testStop));
        when(routeStopRepository.save(any(RouteStop.class))).thenAnswer(invocation -> {
            RouteStop rs = invocation.getArgument(0);
            rs.setId("rs-2");
            return rs;
        });

        RouteStopResponse response = routeStopService.createRouteStop(request);

        assertNotNull(response);
        assertEquals("rs-2", response.getId());
        assertEquals(2, response.getOrder());
        verify(routeStopRepository).save(any(RouteStop.class));
    }

    @Test
    @DisplayName("createRouteStop - Thất bại khi routeId không tồn tại")
    void createRouteStop_RouteNotFound() {
        RouteStopRequest request = RouteStopRequest.builder()
                .routeId("non-existent")
                .stopId("stop-1")
                .order(1)
                .build();

        when(routeRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> routeStopService.createRouteStop(request));
    }

    @Test
    @DisplayName("deleteRouteStop - Xóa thành công")
    void deleteRouteStop_Success() {
        when(routeStopRepository.findById("rs-1")).thenReturn(Optional.of(testRouteStop));

        routeStopService.deleteRouteStop("rs-1");

        verify(routeStopRepository).delete(testRouteStop);
    }
}
