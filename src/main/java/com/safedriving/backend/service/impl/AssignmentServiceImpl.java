package com.safedriving.backend.service.impl;

import com.safedriving.backend.dto.request.AssignmentRequest;
import com.safedriving.backend.dto.response.AssignmentResponse;
import com.safedriving.backend.entity.Assignment;
import com.safedriving.backend.entity.Trip;
import com.safedriving.backend.entity.enums.TripStatus;
import com.safedriving.backend.exception.ResourceNotFoundException;
import com.safedriving.backend.repository.AssignmentRepository;
import com.safedriving.backend.repository.TripRepository;
import com.safedriving.backend.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final TripRepository tripRepository;

    @Override
    public List<AssignmentResponse> getAll() {
        return assignmentRepository.findAllByIsDeletedFalse().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AssignmentResponse getById(String id) {
        Assignment entity = assignmentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phân công ID: " + id));
        return mapToResponse(entity);
    }

    @Override
    public List<AssignmentResponse> getByDriverId(String driverId) {
        return assignmentRepository.findByDriverIdAndIsDeletedFalse(driverId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AssignmentResponse create(AssignmentRequest request) {
        Trip trip = tripRepository.findByIdAndIsDeletedFalse(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến đi ID: " + request.getTripId()));

        Assignment assignment = Assignment.builder()
                .trip(trip)
                .driverId(request.getDriverId())
                .accountId(request.getAccountId())
                .assignmentAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        // Chuyển trạng thái chuyến đi sang ASSIGNED
        trip.setStatus(TripStatus.ASSIGNED);
        tripRepository.save(trip);

        return mapToResponse(assignmentRepository.save(assignment));
    }

    @Override
    @Transactional
    public AssignmentResponse update(String id, AssignmentRequest request) {
        Assignment assignment = assignmentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phân công ID: " + id));

        Trip trip = tripRepository.findByIdAndIsDeletedFalse(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến đi ID: " + request.getTripId()));

        assignment.setTrip(trip);
        assignment.setDriverId(request.getDriverId());
        assignment.setAccountId(request.getAccountId());

        return mapToResponse(assignmentRepository.save(assignment));
    }

    @Override
    @Transactional
    public void delete(String id) {
        Assignment assignment = assignmentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phân công ID: " + id));
        assignment.setIsDeleted(true);
        assignmentRepository.save(assignment);
    }

    private AssignmentResponse mapToResponse(Assignment entity) {
        return AssignmentResponse.builder()
                .id(entity.getId())
                .tripId(entity.getTrip().getId())
                .driverId(entity.getDriverId())
                .accountId(entity.getAccountId())
                .assignmentAt(entity.getAssignmentAt())
                .build();
    }
}
