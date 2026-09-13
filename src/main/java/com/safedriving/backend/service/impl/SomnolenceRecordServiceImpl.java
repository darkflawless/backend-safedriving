package com.safedriving.backend.service.impl;

import com.safedriving.backend.dto.request.SomnolenceRecordRequest;
import com.safedriving.backend.dto.response.SomnolenceRecordResponse;
import com.safedriving.backend.entity.SomnolenceRecord;
import com.safedriving.backend.exception.ResourceNotFoundException;
import com.safedriving.backend.repository.SomnolenceRecordRepository;
import com.safedriving.backend.service.SomnolenceRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SomnolenceRecordServiceImpl implements SomnolenceRecordService {

    private final SomnolenceRecordRepository repository;

    @Override
    public List<SomnolenceRecordResponse> getAll() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SomnolenceRecordResponse getById(Long id) {
        SomnolenceRecord entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản ghi cảnh báo ngủ gật: " + id));
        return mapToResponse(entity);
    }

    @Override
    public List<SomnolenceRecordResponse> getByDriverId(String driverId) {
        return repository.findByDriverId(driverId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SomnolenceRecordResponse create(SomnolenceRecordRequest request) {
        SomnolenceRecord entity = SomnolenceRecord.builder()
                .driverId(request.getDriverId())
                .vehicleId(request.getVehicleId())
                .recordTime(request.getRecordTime() != null ? request.getRecordTime() : LocalDateTime.now())
                .drowsinessLevel(request.getDrowsinessLevel())
                .imageUrl(request.getImageUrl())
                .notes(request.getNotes())
                .build();

        return mapToResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy bản ghi cảnh báo ngủ gật: " + id);
        }
        repository.deleteById(id);
    }

    private SomnolenceRecordResponse mapToResponse(SomnolenceRecord record) {
        return SomnolenceRecordResponse.builder()
                .id(record.getId())
                .driverId(record.getDriverId())
                .vehicleId(record.getVehicleId())
                .recordTime(record.getRecordTime())
                .drowsinessLevel(record.getDrowsinessLevel())
                .imageUrl(record.getImageUrl())
                .notes(record.getNotes())
                .build();
    }
}
