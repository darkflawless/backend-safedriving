package com.safedriving.service.impl;

import com.safedriving.dto.request.TripRequest;
import com.safedriving.dto.response.TripResponse;
import com.safedriving.entity.Route;
import com.safedriving.entity.Trip;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.RouteRepository;
import com.safedriving.repository.TripRepository;
import com.safedriving.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;

    @Override
    public List<TripResponse> getAll() {
        return tripRepository.findAllByIsDeletedFalse().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TripResponse getById(String id) {
        Trip trip = tripRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến đi ID: " + id));
        return mapToResponse(trip);
    }

    @Override
    @Transactional
    public TripResponse create(TripRequest request) {
        if (request.getPlannedEndTime().isBefore(request.getPlannedStartTime())) {
            throw new BadRequestException("Thời gian đến dự kiến không thể trước thời gian xuất phát");
        }

        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tuyến đường ID: " + request.getRouteId()));

        Trip trip = Trip.builder()
                .route(route)
                .plannedStartTime(request.getPlannedStartTime())
                .plannedEndTime(request.getPlannedEndTime())
                .status(request.getStatus() != null ? request.getStatus() : com.safedriving.entity.enums.TripStatus.PLANNED)
                .currentOrder(0)
                .isDeleted(false)
                .build();

        return mapToResponse(tripRepository.save(trip));
    }

    @Override
    @Transactional
    public TripResponse update(String id, TripRequest request) {
        Trip trip = tripRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến đi ID: " + id));

        if (request.getPlannedEndTime().isBefore(request.getPlannedStartTime())) {
            throw new BadRequestException("Thời gian đến dự kiến không thể trước thời gian xuất phát");
        }

        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tuyến đường ID: " + request.getRouteId()));

        trip.setRoute(route);
        trip.setPlannedStartTime(request.getPlannedStartTime());
        trip.setPlannedEndTime(request.getPlannedEndTime());
        if (request.getStatus() != null) trip.setStatus(request.getStatus());

        return mapToResponse(tripRepository.save(trip));
    }

    @Override
    @Transactional
    public void delete(String id) {
        Trip trip = tripRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến đi ID: " + id));
        trip.setIsDeleted(true);
        tripRepository.save(trip);
    }

    private TripResponse mapToResponse(Trip trip) {
        return TripResponse.builder()
                .id(trip.getId())
                .routeId(trip.getRoute() != null ? trip.getRoute().getId() : null)
                .status(trip.getStatus())
                .plannedStartTime(trip.getPlannedStartTime())
                .plannedEndTime(trip.getPlannedEndTime())
                .startTime(trip.getStartTime())
                .endTime(trip.getEndTime())
                .currentOrder(trip.getCurrentOrder())
                .build();
    }
}
