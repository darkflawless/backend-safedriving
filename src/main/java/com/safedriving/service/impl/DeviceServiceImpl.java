package com.safedriving.service.impl;

import com.safedriving.dto.request.DeviceRequest;
import com.safedriving.dto.response.DeviceResponse;
import com.safedriving.entity.Device;
import com.safedriving.entity.enums.DeviceStatus;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.DeviceRepository;
import com.safedriving.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponse> getAllDevices() {
        log.info("Lấy danh sách tất cả thiết bị");
        return deviceRepository.findByIsDeletedFalse().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceResponse getDeviceById(String id) {
        log.info("Tra cứu thiết bị với ID: {}", id);
        Device device = findDeviceOrThrow(id);
        return toResponse(device);
    }

    @Override
    @Transactional
    public DeviceResponse createDevice(DeviceRequest request) {
        log.info("Đăng ký thiết bị mới SN: {}", request.getSerialNumber());

        if (deviceRepository.existsBySerialNumberAndIsDeletedFalse(request.getSerialNumber().trim())) {
            throw new BadRequestException("Số Serial này đã được đăng ký trong hệ thống");
        }

        if (deviceRepository.existsByMacAddressAndIsDeletedFalse(request.getMacAddress().trim())) {
            throw new BadRequestException("Địa chỉ MAC này đã được đăng ký trong hệ thống");
        }

        Device device = Device.builder()
                .serialNumber(request.getSerialNumber().trim())
                .macAddress(request.getMacAddress().trim())
                .deviceStatus(request.getDeviceStatus() != null ? request.getDeviceStatus() : DeviceStatus.ACTIVE)
                .startTime(request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now())
                .isDeleted(false)
                .build();

        Device saved = deviceRepository.save(device);
        log.info("Đăng ký thiết bị thành công với ID: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public DeviceResponse updateDevice(String id, DeviceRequest request) {
        log.info("Cập nhật thông tin thiết bị với ID: {}", id);
        Device device = findDeviceOrThrow(id);

        if (request.getSerialNumber() != null && !request.getSerialNumber().isBlank()) {
            String newSn = request.getSerialNumber().trim();
            if (!device.getSerialNumber().equals(newSn) && deviceRepository.existsBySerialNumberAndIdNotAndIsDeletedFalse(newSn, id)) {
                throw new BadRequestException("Số Serial này đã được đăng ký cho thiết bị khác");
            }
            device.setSerialNumber(newSn);
        }

        if (request.getMacAddress() != null && !request.getMacAddress().isBlank()) {
            String newMac = request.getMacAddress().trim();
            if (!device.getMacAddress().equals(newMac) && deviceRepository.existsByMacAddressAndIdNotAndIsDeletedFalse(newMac, id)) {
                throw new BadRequestException("Địa chỉ MAC này đã được đăng ký cho thiết bị khác");
            }
            device.setMacAddress(newMac);
        }

        if (request.getDeviceStatus() != null) {
            device.setDeviceStatus(request.getDeviceStatus());
        }

        if (request.getStartTime() != null) {
            device.setStartTime(request.getStartTime());
        }

        Device updated = deviceRepository.save(device);
        log.info("Cập nhật thiết bị thành công cho ID: {}", updated.getId());
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteDevice(String id) {
        log.info("Xóa mềm thiết bị với ID: {}", id);
        Device device = findDeviceOrThrow(id);
        device.setIsDeleted(true);
        deviceRepository.save(device);
        log.info("Đã xóa mềm thiết bị với ID: {}", id);
    }

    private Device findDeviceOrThrow(String id) {
        return deviceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị với ID: " + id));
    }

    private DeviceResponse toResponse(Device device) {
        return DeviceResponse.builder()
                .id(device.getId())
                .serialNumber(device.getSerialNumber())
                .macAddress(device.getMacAddress())
                .deviceStatus(device.getDeviceStatus())
                .startTime(device.getStartTime())
                .build();
    }
}
