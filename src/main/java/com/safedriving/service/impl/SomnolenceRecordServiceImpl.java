package com.safedriving.service.impl;

import com.safedriving.dto.request.SomnolenceRecordRequest;
import com.safedriving.dto.response.SomnolenceRecordResponse;
import com.safedriving.entity.Driver;
import com.safedriving.entity.SomnolenceRecord;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.DriverRepository;
import com.safedriving.repository.SomnolenceRecordRepository;
import com.safedriving.service.SomnolenceRecordService;
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
    private final DriverRepository driverRepository;

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
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài xế ID: " + request.getDriverId()));

        SomnolenceRecord entity = SomnolenceRecord.builder()
                .driver(driver)
                .recordTime(request.getRecordTime() != null ? request.getRecordTime() : LocalDateTime.now())
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
                .driverId(record.getDriver() != null ? record.getDriver().getId() : null)
                .recordTime(record.getRecordTime())
                .notes(record.getNotes())
                .build();
    }
}
