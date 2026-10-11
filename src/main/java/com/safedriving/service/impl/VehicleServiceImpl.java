package com.safedriving.service.impl;

import com.safedriving.dto.request.VehicleRequest;
import com.safedriving.dto.response.VehicleResponse;
import com.safedriving.entity.Vehicle;
import com.safedriving.entity.VehicleType;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.VehicleRepository;
import com.safedriving.repository.VehicleTypeRepository;
import com.safedriving.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleTypeRepository vehicleTypeRepository;

    @Override
    public List<VehicleResponse> getAll() {
        return vehicleRepository.findAllByIsDeletedFalse().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VehicleResponse getById(String id) {
        Vehicle vehicle = vehicleRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe có ID: " + id));
        return mapToResponse(vehicle);
    }

    @Override
    @Transactional
    public VehicleResponse create(VehicleRequest request) {
        if (vehicleRepository.existsByPlateNumberAndIsDeletedFalse(request.getPlateNumber())) {
            throw new BadRequestException("Biển số xe đã tồn tại trong hệ thống: " + request.getPlateNumber());
        }
        VehicleType type = vehicleTypeRepository.findById(request.getVehicleTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại xe ID: " + request.getVehicleTypeId()));
        Vehicle vehicle = Vehicle.builder()
                .plateNumber(request.getPlateNumber())
                .vin(request.getVin())
                .capacity(request.getCapacity() != null ? request.getCapacity() : type.getCapacity())
                .vehicleType(type)
                .status(request.getStatus() != null ? request.getStatus() : com.safedriving.entity.enums.VehicleStatus.AVAILABLE)
                .odometerKm(request.getOdometerKm() != null ? request.getOdometerKm() : 0.0)
                .isDeleted(false)
                .build();
        return mapToResponse(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional
    public VehicleResponse update(String id, VehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe có ID: " + id));
        VehicleType type = vehicleTypeRepository.findById(request.getVehicleTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại xe ID: " + request.getVehicleTypeId()));
        if (!vehicle.getPlateNumber().equals(request.getPlateNumber())
                && vehicleRepository.existsByPlateNumberAndIsDeletedFalse(request.getPlateNumber())) {
            throw new BadRequestException("Biển số xe đã được dùng bởi xe khác: " + request.getPlateNumber());
        }
        vehicle.setPlateNumber(request.getPlateNumber());
        vehicle.setVin(request.getVin());
        vehicle.setCapacity(request.getCapacity());
        vehicle.setVehicleType(type);
        if (request.getStatus() != null) vehicle.setStatus(request.getStatus());
        if (request.getOdometerKm() != null) vehicle.setOdometerKm(request.getOdometerKm());
        return mapToResponse(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional
    public void delete(String id) {
        Vehicle vehicle = vehicleRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe có ID: " + id));
        vehicle.setIsDeleted(true); // Xóa mềm
        vehicleRepository.save(vehicle);
    }

    private VehicleResponse mapToResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .plateNumber(vehicle.getPlateNumber())
                .vin(vehicle.getVin())
                .capacity(vehicle.getCapacity())
                .vehicleTypeId(vehicle.getVehicleType().getId())
                .vehicleTypeName(vehicle.getVehicleType().getName())
                .status(vehicle.getStatus())
                .odometerKm(vehicle.getOdometerKm())
                .build();
    }
}
