package com.safedriving.service;

import com.safedriving.dto.request.RouteRequest;
import com.safedriving.dto.response.RouteResponse;
import com.safedriving.entity.Route;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.RouteRepository;
import com.safedriving.service.impl.RouteServiceImpl;
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
class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private RouteServiceImpl routeService;

    private Route testRoute;

    @BeforeEach
    void setUp() {
        testRoute = Route.builder()
                .id("route-1")
                .code("HN-HP-01")
                .routeName("Hà Nội - Hải Phòng")
                .distanceKm(105.0)
                .standardDurationMin(90)
                .isActive(true)
                .note("Tuyến cao tốc 5B")
                .build();
    }

    @Test
    @DisplayName("getAllRoutes - Lấy toàn bộ danh sách tuyến đường thành công")
    void getAllRoutes_Success() {
        when(routeRepository.findAll()).thenReturn(List.of(testRoute));

        List<RouteResponse> results = routeService.getAllRoutes();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("HN-HP-01", results.get(0).getCode());
    }

    @Test
    @DisplayName("getRouteById - Lấy chi tiết tuyến đường khi ID tồn tại")
    void getRouteById_Success() {
        when(routeRepository.findById("route-1")).thenReturn(Optional.of(testRoute));

        RouteResponse response = routeService.getRouteById("route-1");

        assertNotNull(response);
        assertEquals("route-1", response.getId());
        assertEquals("Hà Nội - Hải Phòng", response.getRouteName());
    }

    @Test
    @DisplayName("getRouteById - Ném ResourceNotFoundException khi ID không tồn tại")
    void getRouteById_NotFound() {
        when(routeRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> routeService.getRouteById("non-existent"));
    }

    @Test
    @DisplayName("createRoute - Thành công lưu tuyến đường mới")
    void createRoute_Success() {
        RouteRequest request = RouteRequest.builder()
                .code("HN-QN-01")
                .routeName("Hà Nội - Quảng Ninh")
                .distanceKm(150.0)
                .standardDurationMin(120)
                .build();

        when(routeRepository.existsByCode("HN-QN-01")).thenReturn(false);
        when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> {
            Route r = invocation.getArgument(0);
            r.setId("route-2");
            return r;
        });

        RouteResponse response = routeService.createRoute(request);

        assertNotNull(response);
        assertEquals("route-2", response.getId());
        assertEquals("HN-QN-01", response.getCode());
        verify(routeRepository).save(any(Route.class));
    }

    @Test
    @DisplayName("createRoute - Thất bại ném BadRequestException khi mã code đã tồn tại")
    void createRoute_DuplicateCode() {
        RouteRequest request = RouteRequest.builder()
                .code("HN-HP-01")
                .routeName("Tuyến trùng code")
                .distanceKm(100.0)
                .standardDurationMin(80)
                .build();

        when(routeRepository.existsByCode("HN-HP-01")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> routeService.createRoute(request));
    }

    @Test
    @DisplayName("updateRoute - Cập nhật thông tin tuyến đường thành công")
    void updateRoute_Success() {
        RouteRequest request = RouteRequest.builder()
                .code("HN-HP-01")
                .routeName("Hà Nội - Hải Phòng Mới")
                .distanceKm(110.0)
                .standardDurationMin(95)
                .build();

        when(routeRepository.findById("route-1")).thenReturn(Optional.of(testRoute));
        when(routeRepository.save(any(Route.class))).thenReturn(testRoute);

        RouteResponse response = routeService.updateRoute("route-1", request);

        assertNotNull(response);
        assertEquals("Hà Nội - Hải Phòng Mới", response.getRouteName());
        assertEquals(110.0, response.getDistanceKm());
    }

    @Test
    @DisplayName("deleteRoute - Xóa tuyến đường thành công")
    void deleteRoute_Success() {
        when(routeRepository.findById("route-1")).thenReturn(Optional.of(testRoute));

        routeService.deleteRoute("route-1");

        verify(routeRepository).delete(testRoute);
    }
}
