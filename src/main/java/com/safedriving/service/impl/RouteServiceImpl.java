package com.safedriving.service.impl;

import com.safedriving.dto.request.RouteRequest;
import com.safedriving.dto.response.RouteResponse;
import com.safedriving.entity.Route;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.RouteRepository;
import com.safedriving.service.RouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RouteResponse> getAllRoutes() {
        log.info("Lấy danh sách tất cả các tuyến đường");
        return routeRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RouteResponse getRouteById(String id) {
        log.info("Tra cứu thông tin tuyến đường với ID: {}", id);
        Route route = findRouteOrThrow(id);
        return toResponse(route);
    }

    @Override
    @Transactional
    public RouteResponse createRoute(RouteRequest request) {
        log.info("Tạo mới tuyến đường với mã: {}", request.getCode());
        String code = request.getCode().trim();
        if (routeRepository.existsByCode(code)) {
            throw new BadRequestException("Mã tuyến đường '" + code + "' đã tồn tại trong hệ thống");
        }

        Route route = Route.builder()
                .code(code)
                .routeName(request.getRouteName().trim())
                .distanceKm(request.getDistanceKm())
                .standardDurationMin(request.getStandardDurationMin())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .note(request.getNote())
                .build();

        Route saved = routeRepository.save(route);
        log.info("Tạo tuyến đường thành công với ID: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public RouteResponse updateRoute(String id, RouteRequest request) {
        log.info("Cập nhật tuyến đường với ID: {}", id);
        Route route = findRouteOrThrow(id);

        String newCode = request.getCode().trim();
        if (!route.getCode().equalsIgnoreCase(newCode) && routeRepository.existsByCode(newCode)) {
            throw new BadRequestException("Mã tuyến đường '" + newCode + "' đã tồn tại trong hệ thống");
        }

        route.setCode(newCode);
        route.setRouteName(request.getRouteName().trim());
        route.setDistanceKm(request.getDistanceKm());
        route.setStandardDurationMin(request.getStandardDurationMin());
        if (request.getIsActive() != null) {
            route.setIsActive(request.getIsActive());
        }
        route.setNote(request.getNote());

        Route updated = routeRepository.save(route);
        log.info("Cập nhật tuyến đường thành công cho ID: {}", updated.getId());
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteRoute(String id) {
        log.info("Xóa tuyến đường với ID: {}", id);
        Route route = findRouteOrThrow(id);
        routeRepository.delete(route);
        log.info("Đã xóa tuyến đường với ID: {}", id);
    }

    private Route findRouteOrThrow(String id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tuyến đường với ID: " + id));
    }

    private RouteResponse toResponse(Route route) {
        return RouteResponse.builder()
                .id(route.getId())
                .code(route.getCode())
                .routeName(route.getRouteName())
                .distanceKm(route.getDistanceKm())
                .standardDurationMin(route.getStandardDurationMin())
                .isActive(route.getIsActive())
                .note(route.getNote())
                .build();
    }
}
