package com.safedriving.service.impl;

import com.safedriving.dto.request.AssignDriverRequest;
import com.safedriving.dto.request.VehicleRequest;
import com.safedriving.dto.response.VehicleResponse;
import com.safedriving.entity.Device;
import com.safedriving.entity.Driver;
import com.safedriving.entity.Staff;
import com.safedriving.entity.Vehicle;
import com.safedriving.entity.VehicleType;
import com.safedriving.entity.enums.VehicleStatus;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.DeviceRepository;
import com.safedriving.repository.DriverRepository;
import com.safedriving.repository.VehicleRepository;
import com.safedriving.repository.VehicleTypeRepository;
import com.safedriving.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Triển khai nghiệp vụ Quản lý Phương tiện và Đội xe.
 * Tích hợp các chức năng CRUD (@author phanthuy0811) và phân công tài xế / tracking.
 *
 * @author phanthuy0811
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final DriverRepository driverRepository;
    private final DeviceRepository deviceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicles(VehicleStatus status) {
        List<Vehicle> vehicles;
        if (status != null) {
            vehicles = vehicleRepository.findByStatusAndIsDeletedFalse(status);
        } else {
            vehicles = vehicleRepository.findByIsDeletedFalse();
        }
        return vehicles.stream().map(this::toVehicleResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(String id) {
        Vehicle vehicle = vehicleRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phương tiện với ID: " + id));
        return toVehicleResponse(vehicle);
    }

    @Override
    @Transactional
    public VehicleResponse createVehicle(VehicleRequest request) {
        if (vehicleRepository.existsByPlateNumberAndIsDeletedFalse(request.getPlateNumber())) {
            throw new BadRequestException("Biển số xe đã tồn tại trong hệ thống: " + request.getPlateNumber());
        }

        VehicleType vehicleType = vehicleTypeRepository.findById(request.getVehicleTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại xe ID: " + request.getVehicleTypeId()));

        Device device = null;
        if (request.getDeviceId() != null && !request.getDeviceId().isBlank()) {
            device = deviceRepository.findByIdAndIsDeletedFalse(request.getDeviceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị với ID: " + request.getDeviceId()));
        }

        Vehicle vehicle = Vehicle.builder()
                .plateNumber(request.getPlateNumber().trim().toUpperCase())
                .vin(request.getVin())
                .capacity(request.getCapacity() != null ? request.getCapacity() : vehicleType.getCapacity())
                .vehicleType(vehicleType)
                .status(request.getStatus() != null ? request.getStatus() : VehicleStatus.AVAILABLE)
                .odometerKm(request.getOdometerKm() != null ? request.getOdometerKm() : 0.0)
                .device(device)
                .isDeleted(false)
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("✅ Đã tạo mới phương tiện: {} (ID: {})", saved.getPlateNumber(), saved.getId());
        return toVehicleResponse(saved);
    }

    @Override
    @Transactional
    public VehicleResponse updateVehicle(String id, VehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phương tiện với ID: " + id));

        String formattedPlate = request.getPlateNumber().trim().toUpperCase();
        if (vehicleRepository.existsByPlateNumberAndIdNotAndIsDeletedFalse(formattedPlate, id)) {
            throw new BadRequestException("Biển số xe đã được dùng bởi xe khác: " + formattedPlate);
        }

        VehicleType vehicleType = vehicleTypeRepository.findById(request.getVehicleTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại xe ID: " + request.getVehicleTypeId()));

        Device device = vehicle.getDevice();
        if (request.getDeviceId() != null) {
            if (request.getDeviceId().isBlank()) {
                device = null;
            } else {
                device = deviceRepository.findByIdAndIsDeletedFalse(request.getDeviceId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị với ID: " + request.getDeviceId()));
            }
        }

        vehicle.setPlateNumber(formattedPlate);
        vehicle.setVin(request.getVin());
        vehicle.setCapacity(request.getCapacity() != null ? request.getCapacity() : vehicleType.getCapacity());
        vehicle.setVehicleType(vehicleType);
        if (request.getStatus() != null) {
            vehicle.setStatus(request.getStatus());
        }
        if (request.getOdometerKm() != null) {
            vehicle.setOdometerKm(request.getOdometerKm());
        }
        vehicle.setDevice(device);

        Vehicle updated = vehicleRepository.save(vehicle);
        log.info("✅ Đã cập nhật phương tiện: {} (ID: {})", updated.getPlateNumber(), updated.getId());
        return toVehicleResponse(updated);
    }

    @Override
    @Transactional
    public void deleteVehicle(String id) {
        Vehicle vehicle = vehicleRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phương tiện với ID: " + id));

        vehicle.setIsDeleted(true);
        vehicleRepository.save(vehicle);
        log.info("🗑️ Đã xóa mềm phương tiện ID: {}", id);
    }

    @Override
    @Transactional
    public VehicleResponse assignDriver(String vehicleId, AssignDriverRequest request) {
        Vehicle vehicle = vehicleRepository.findByIdAndIsDeletedFalse(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phương tiện với ID: " + vehicleId));

        Driver newDriver = driverRepository.findByIdAndIsDeletedFalse(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài xế với ID: " + request.getDriverId()));

        // Nếu xe này đang có tài xế cũ, bỏ gán xe cho tài xế cũ
        Optional<Driver> currentDriverOpt = driverRepository.findByVehicleIdAndIsDeletedFalse(vehicleId);
        currentDriverOpt.ifPresent(oldDriver -> {
            if (!oldDriver.getId().equals(newDriver.getId())) {
                oldDriver.setVehicle(null);
                driverRepository.save(oldDriver);
                log.info("Đã hủy gán xe {} từ tài xế cũ {}", vehicleId, oldDriver.getId());
            }
        });

        // Gán xe mới cho tài xế
        newDriver.setVehicle(vehicle);
        driverRepository.save(newDriver);
        log.info("✅ Đã phân công xe {} ({}) cho tài xế {}", vehicleId, vehicle.getPlateNumber(), newDriver.getId());

        return toVehicleResponse(vehicle);
    }

    private VehicleResponse toVehicleResponse(Vehicle vehicle) {
        String driverId = null;
        String driverName = null;

        Optional<Driver> driverOpt = driverRepository.findByVehicleIdAndIsDeletedFalse(vehicle.getId());
        if (driverOpt.isPresent()) {
            Driver driver = driverOpt.get();
            driverId = driver.getId();
            Staff staff = driver.getStaff();
            if (staff != null) {
                driverName = staff.getLastName() + " " + staff.getFirstName();
            }
        }

        Short typeId = (vehicle.getVehicleType() != null) ? vehicle.getVehicleType().getId() : null;
        String typeName = (vehicle.getVehicleType() != null) ? vehicle.getVehicleType().getName() : null;
        String deviceId = (vehicle.getDevice() != null) ? vehicle.getDevice().getId() : null;

        return VehicleResponse.builder()
                .id(vehicle.getId())
                .plateNumber(vehicle.getPlateNumber())
                .vin(vehicle.getVin())
                .capacity(vehicle.getCapacity())
                .vehicleTypeId(typeId)
                .vehicleTypeName(typeName)
                .status(vehicle.getStatus())
                .deviceId(deviceId)
                .odometerKm(vehicle.getOdometerKm())
                .assignedDriverId(driverId)
                .assignedDriverName(driverName)
                .build();
    }
}
