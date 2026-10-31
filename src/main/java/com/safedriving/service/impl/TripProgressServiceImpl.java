package com.safedriving.service.impl;

import com.safedriving.dto.request.TripProgressRequest;
import com.safedriving.dto.response.TripProgressResponse;
import com.safedriving.entity.Trip;
import com.safedriving.entity.TripProgress;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.TripProgressRepository;
import com.safedriving.repository.TripRepository;
import com.safedriving.service.TripProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripProgressServiceImpl implements TripProgressService {

    private final TripProgressRepository tripProgressRepository;
    private final TripRepository tripRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TripProgressResponse> getAllTripProgress(String tripId) {
        if (tripId != null && !tripId.isBlank()) {
            log.info("Lấy tiến độ hành trình cho chuyến đi ID: {}", tripId);
            return tripProgressRepository.findByTripIdOrderByOrderAsc(tripId.trim()).stream()
                    .map(this::toResponse)
                    .toList();
        }
        log.info("Lấy tất cả nhật ký tiến độ chuyến đi");
        return tripProgressRepository.findAllByOrderByArriveDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TripProgressResponse getTripProgressById(String id) {
        log.info("Tra cứu tiến độ chuyến đi với ID: {}", id);
        TripProgress tripProgress = findTripProgressOrThrow(id);
        return toResponse(tripProgress);
    }

    @Override
    @Transactional
    public TripProgressResponse createTripProgress(TripProgressRequest request) {
        log.info("Ghi nhận tiến độ mới cho chuyến đi ID: {}, thứ tự trạm: {}", request.getTripId(), request.getOrder());
        Trip trip = tripRepository.findByIdAndIsDeletedFalse(request.getTripId().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến đi với ID: " + request.getTripId()));

        TripProgress tripProgress = TripProgress.builder()
                .trip(trip)
                .order(request.getOrder())
                .arrive(request.getArrive() != null ? request.getArrive() : LocalDateTime.now())
                .leave(request.getLeave())
                .build();

        TripProgress saved = tripProgressRepository.save(tripProgress);
        log.info("Ghi nhận tiến độ thành công với ID: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public TripProgressResponse updateTripProgress(String id, TripProgressRequest request) {
        log.info("Cập nhật tiến độ chuyến đi với ID: {}", id);
        TripProgress tripProgress = findTripProgressOrThrow(id);

        if (!tripProgress.getTrip().getId().equals(request.getTripId().trim())) {
            Trip trip = tripRepository.findByIdAndIsDeletedFalse(request.getTripId().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến đi với ID: " + request.getTripId()));
            tripProgress.setTrip(trip);
        }

        tripProgress.setOrder(request.getOrder());
        if (request.getArrive() != null) {
            tripProgress.setArrive(request.getArrive());
        }
        tripProgress.setLeave(request.getLeave());

        TripProgress updated = tripProgressRepository.save(tripProgress);
        log.info("Cập nhật tiến độ thành công cho ID: {}", updated.getId());
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTripProgress(String id) {
        log.info("Xóa bản ghi tiến độ chuyến đi với ID: {}", id);
        TripProgress tripProgress = findTripProgressOrThrow(id);
        tripProgressRepository.delete(tripProgress);
        log.info("Đã xóa bản ghi tiến độ chuyến đi với ID: {}", id);
    }

    private TripProgress findTripProgressOrThrow(String id) {
        return tripProgressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tiến độ chuyến đi với ID: " + id));
    }

    private TripProgressResponse toResponse(TripProgress tripProgress) {
        return TripProgressResponse.builder()
                .id(tripProgress.getId())
                .tripId(tripProgress.getTrip() != null ? tripProgress.getTrip().getId() : null)
                .order(tripProgress.getOrder())
                .arrive(tripProgress.getArrive())
                .leave(tripProgress.getLeave())
                .build();
    }
}
