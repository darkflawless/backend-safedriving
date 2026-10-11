package com.safedriving.service.impl;

import com.safedriving.dto.request.AlcoholRecordRequest;
import com.safedriving.dto.response.AlcoholRecordResponse;
import com.safedriving.entity.AlcoholRecord;
import com.safedriving.entity.Driver;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.AlcoholRecordRepository;
import com.safedriving.repository.DriverRepository;
import com.safedriving.service.AlcoholRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlcoholRecordServiceImpl implements AlcoholRecordService {

    private final AlcoholRecordRepository alcoholRecordRepository;
    private final DriverRepository driverRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AlcoholRecordResponse> getAllAlcoholRecords(String driverId) {
        if (driverId != null && !driverId.isBlank()) {
            log.info("Lấy danh sách bản ghi nồng độ cồn của tài xế ID: {}", driverId);
            return alcoholRecordRepository.findByDriverIdOrderByMeasurementTimeDesc(driverId).stream()
                    .map(this::toResponse)
                    .toList();
        }
        log.info("Lấy danh sách tất cả bản ghi nồng độ cồn");
        return alcoholRecordRepository.findAll(Sort.by(Sort.Direction.DESC, "measurementTime")).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AlcoholRecordResponse getAlcoholRecordById(Long id) {
        log.info("Tra cứu bản ghi nồng độ cồn ID: {}", id);
        AlcoholRecord record = findAlcoholRecordOrThrow(id);
        return toResponse(record);
    }

    @Override
    @Transactional
    public AlcoholRecordResponse createAlcoholRecord(AlcoholRecordRequest request) {
        log.info("Ghi nhận nồng độ cồn cho tài xế ID: {}", request.getDriverId());

        if (request.getDriverId() == null || request.getDriverId().isBlank()) {
            throw new BadRequestException("ID tài xế là bắt buộc");
        }

        Driver driver = driverRepository.findByIdAndIsDeletedFalse(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài xế với ID: " + request.getDriverId()));

        AlcoholRecord record = AlcoholRecord.builder()
                .driver(driver)
                .alcoholLevel(request.getAlcoholLevel())
                .measurementTime(request.getMeasurementTime() != null ? request.getMeasurementTime() : LocalDateTime.now())
                .notes(request.getNotes())
                .build();

        AlcoholRecord saved = alcoholRecordRepository.save(record);
        log.info("Đã ghi nhận nồng độ cồn thành công, Record ID: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AlcoholRecordResponse updateAlcoholRecord(Long id, AlcoholRecordRequest request) {
        log.info("Cập nhật bản ghi nồng độ cồn ID: {}", id);
        AlcoholRecord record = findAlcoholRecordOrThrow(id);

        if (request.getAlcoholLevel() != null) {
            record.setAlcoholLevel(request.getAlcoholLevel());
        }

        if (request.getMeasurementTime() != null) {
            record.setMeasurementTime(request.getMeasurementTime());
        }

        if (request.getNotes() != null) {
            record.setNotes(request.getNotes());
        }

        if (request.getDriverId() != null && !request.getDriverId().isBlank() && !record.getDriver().getId().equals(request.getDriverId())) {
            Driver driver = driverRepository.findByIdAndIsDeletedFalse(request.getDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài xế với ID: " + request.getDriverId()));
            record.setDriver(driver);
        }

        AlcoholRecord updated = alcoholRecordRepository.save(record);
        log.info("Đã cập nhật bản ghi nồng độ cồn thành công cho ID: {}", updated.getId());
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAlcoholRecord(Long id) {
        log.info("Xóa bản ghi nồng độ cồn ID: {}", id);
        AlcoholRecord record = findAlcoholRecordOrThrow(id);
        alcoholRecordRepository.delete(record);
        log.info("Đã xóa bản ghi nồng độ cồn ID: {}", id);
    }

    private AlcoholRecord findAlcoholRecordOrThrow(Long id) {
        return alcoholRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản ghi nồng độ cồn với ID: " + id));
    }

    private AlcoholRecordResponse toResponse(AlcoholRecord record) {
        String driverName = null;
        if (record.getDriver() != null && record.getDriver().getStaff() != null) {
            driverName = record.getDriver().getStaff().getFirstName() + " " + record.getDriver().getStaff().getLastName();
        }

        return AlcoholRecordResponse.builder()
                .id(record.getId())
                .driverId(record.getDriver() != null ? record.getDriver().getId() : null)
                .driverName(driverName)
                .alcoholLevel(record.getAlcoholLevel())
                .measurementTime(record.getMeasurementTime())
                .notes(record.getNotes())
                .build();
    }
}
