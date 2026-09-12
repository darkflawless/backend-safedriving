package com.safedriving.service.impl;

import com.safedriving.dto.request.RouteStopRequest;
import com.safedriving.dto.response.AddressResponse;
import com.safedriving.dto.response.RouteStopResponse;
import com.safedriving.dto.response.StopResponse;
import com.safedriving.entity.Address;
import com.safedriving.entity.Route;
import com.safedriving.entity.RouteStop;
import com.safedriving.entity.Stop;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.RouteRepository;
import com.safedriving.repository.RouteStopRepository;
import com.safedriving.repository.StopRepository;
import com.safedriving.service.RouteStopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteStopServiceImpl implements RouteStopService {

    private final RouteStopRepository routeStopRepository;
    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RouteStopResponse> getAllRouteStops(String routeId) {
        if (routeId != null && !routeId.isBlank()) {
            log.info("Lấy danh sách điểm dừng cho tuyến ID: {}", routeId);
            return routeStopRepository.findByRouteIdOrderByOrderAsc(routeId.trim()).stream()
                    .map(this::toResponse)
                    .toList();
        }
        log.info("Lấy tất cả liên kết điểm dừng trên tuyến");
        return routeStopRepository.findAllByOrderByRouteIdAscOrderAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RouteStopResponse getRouteStopById(String id) {
        log.info("Tra cứu liên kết điểm dừng trên tuyến với ID: {}", id);
        RouteStop routeStop = findRouteStopOrThrow(id);
        return toResponse(routeStop);
    }

    @Override
    @Transactional
    public RouteStopResponse createRouteStop(RouteStopRequest request) {
        log.info("Thêm điểm dừng vào tuyến: routeId={}, stopId={}, order={}",
                request.getRouteId(), request.getStopId(), request.getOrder());

        Route route = routeRepository.findById(request.getRouteId().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tuyến đường với ID: " + request.getRouteId()));

        Stop stop = stopRepository.findById(request.getStopId().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy điểm dừng với ID: " + request.getStopId()));

        RouteStop routeStop = RouteStop.builder()
                .route(route)
                .stop(stop)
                .order(request.getOrder())
                .build();

        RouteStop saved = routeStopRepository.save(routeStop);
        log.info("Tạo liên kết điểm dừng trên tuyến thành công với ID: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public RouteStopResponse updateRouteStop(String id, RouteStopRequest request) {
        log.info("Cập nhật liên kết điểm dừng trên tuyến với ID: {}", id);
        RouteStop routeStop = findRouteStopOrThrow(id);

        if (!routeStop.getRoute().getId().equals(request.getRouteId().trim())) {
            Route route = routeRepository.findById(request.getRouteId().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tuyến đường với ID: " + request.getRouteId()));
            routeStop.setRoute(route);
        }

        if (!routeStop.getStop().getId().equals(request.getStopId().trim())) {
            Stop stop = stopRepository.findById(request.getStopId().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy điểm dừng với ID: " + request.getStopId()));
            routeStop.setStop(stop);
        }

        routeStop.setOrder(request.getOrder());

        RouteStop updated = routeStopRepository.save(routeStop);
        log.info("Cập nhật liên kết điểm dừng thành công cho ID: {}", updated.getId());
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteRouteStop(String id) {
        log.info("Xóa liên kết điểm dừng trên tuyến với ID: {}", id);
        RouteStop routeStop = findRouteStopOrThrow(id);
        routeStopRepository.delete(routeStop);
        log.info("Đã xóa liên kết điểm dừng trên tuyến với ID: {}", id);
    }

    private RouteStop findRouteStopOrThrow(String id) {
        return routeStopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy điểm dừng trên tuyến với ID: " + id));
    }

    private RouteStopResponse toResponse(RouteStop routeStop) {
        Stop stop = routeStop.getStop();
        StopResponse stopResponse = null;
        if (stop != null) {
            Address address = stop.getAddress();
            AddressResponse addressResponse = null;
            if (address != null) {
                addressResponse = AddressResponse.builder()
                        .id(address.getId())
                        .exactAddress(address.getExactAddress())
                        .commune(address.getCommune())
                        .province(address.getProvince())
                        .lat(address.getLat())
                        .lng(address.getLng())
                        .build();
            }
            stopResponse = StopResponse.builder()
                    .id(stop.getId())
                    .nameStop(stop.getNameStop())
                    .address(addressResponse)
                    .type(stop.getType())
                    .build();
        }

        return RouteStopResponse.builder()
                .id(routeStop.getId())
                .routeId(routeStop.getRoute() != null ? routeStop.getRoute().getId() : null)
                .routeCode(routeStop.getRoute() != null ? routeStop.getRoute().getCode() : null)
                .routeName(routeStop.getRoute() != null ? routeStop.getRoute().getRouteName() : null)
                .stop(stopResponse)
                .order(routeStop.getOrder())
                .build();
    }
}
