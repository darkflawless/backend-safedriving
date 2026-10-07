package com.safedriving.service.impl;

import com.safedriving.dto.request.AssignDriverRequest;
import com.safedriving.dto.response.VehicleResponse;
import com.safedriving.entity.Driver;
import com.safedriving.entity.Staff;
import com.safedriving.entity.Vehicle;
import com.safedriving.entity.enums.VehicleStatus;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.DriverRepository;
import com.safedriving.repository.VehicleRepository;
import com.safedriving.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

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

        String typeName = (vehicle.getVehicleType() != null) ? vehicle.getVehicleType().getName() : null;
        String deviceId = (vehicle.getDevice() != null) ? vehicle.getDevice().getId() : null;

        return VehicleResponse.builder()
                .id(vehicle.getId())
                .plateNumber(vehicle.getPlateNumber())
                .vin(vehicle.getVin())
                .capacity(vehicle.getCapacity())
                .vehicleTypeName(typeName)
                .status(vehicle.getStatus())
                .deviceId(deviceId)
                .odometerKm(vehicle.getOdometerKm())
                .assignedDriverId(driverId)
                .assignedDriverName(driverName)
                .build();
    }
}
