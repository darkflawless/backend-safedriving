package com.safedriving.service.impl;

import com.safedriving.dto.request.VehicleLogRequest;
import com.safedriving.dto.response.VehicleLogResponse;
import com.safedriving.entity.Vehicle;
import com.safedriving.entity.VehicleLog;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.VehicleLogRepository;
import com.safedriving.repository.VehicleRepository;
import com.safedriving.service.VehicleLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleLogServiceImpl implements VehicleLogService {

    private final VehicleLogRepository vehicleLogRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VehicleLogResponse> getAllVehicleLogs(String vehicleId) {
        if (vehicleId != null && !vehicleId.isBlank()) {
            log.info("Lấy danh sách nhật ký hành trình cho phương tiện ID: {}", vehicleId);
            return vehicleLogRepository.findByVehicleIdOrderByTimeVehicleLogDesc(vehicleId.trim()).stream()
                    .map(this::toResponse)
                    .toList();
        }
        log.info("Lấy tất cả nhật ký hành trình");
        return vehicleLogRepository.findAllByOrderByTimeVehicleLogDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleLogResponse getVehicleLogById(Long id) {
        log.info("Tra cứu nhật ký phương tiện với ID: {}", id);
        VehicleLog logEntry = findVehicleLogOrThrow(id);
        return toResponse(logEntry);
    }

    @Override
    @Transactional
    public VehicleLogResponse createVehicleLog(VehicleLogRequest request) {
        log.info("Tạo nhật ký mới cho phương tiện ID: {}", request.getVehicleId());
        Vehicle vehicle = vehicleRepository.findByIdAndIsDeletedFalse(request.getVehicleId().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phương tiện với ID: " + request.getVehicleId()));

        VehicleLog vehicleLog = VehicleLog.builder()
                .vehicle(vehicle)
                .timeVehicleLog(request.getTimeVehicleLog() != null ? request.getTimeVehicleLog() : LocalDateTime.now())
                .lat(request.getLat())
                .lng(request.getLng())
                .build();

        VehicleLog saved = vehicleLogRepository.save(vehicleLog);
        log.info("Tạo nhật ký phương tiện thành công với ID: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public VehicleLogResponse updateVehicleLog(Long id, VehicleLogRequest request) {
        log.info("Cập nhật nhật ký phương tiện với ID: {}", id);
        VehicleLog logEntry = findVehicleLogOrThrow(id);

        if (!logEntry.getVehicle().getId().equals(request.getVehicleId())) {
            Vehicle vehicle = vehicleRepository.findByIdAndIsDeletedFalse(request.getVehicleId().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phương tiện với ID: " + request.getVehicleId()));
            logEntry.setVehicle(vehicle);
        }

        if (request.getTimeVehicleLog() != null) {
            logEntry.setTimeVehicleLog(request.getTimeVehicleLog());
        }
        logEntry.setLat(request.getLat());
        logEntry.setLng(request.getLng());

        VehicleLog updated = vehicleLogRepository.save(logEntry);
        log.info("Cập nhật nhật ký phương tiện thành công cho ID: {}", updated.getId());
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteVehicleLog(Long id) {
        log.info("Xóa nhật ký phương tiện với ID: {}", id);
        VehicleLog logEntry = findVehicleLogOrThrow(id);
        vehicleLogRepository.delete(logEntry);
        log.info("Đã xóa nhật ký phương tiện với ID: {}", id);
    }

    private VehicleLog findVehicleLogOrThrow(Long id) {
        return vehicleLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhật ký phương tiện với ID: " + id));
    }

    private VehicleLogResponse toResponse(VehicleLog vehicleLog) {
        return VehicleLogResponse.builder()
                .id(vehicleLog.getId())
                .vehicleId(vehicleLog.getVehicle() != null ? vehicleLog.getVehicle().getId() : null)
                .plateNumber(vehicleLog.getVehicle() != null ? vehicleLog.getVehicle().getPlateNumber() : null)
                .timeVehicleLog(vehicleLog.getTimeVehicleLog())
                .lat(vehicleLog.getLat())
                .lng(vehicleLog.getLng())
                .build();
    }
}
