package com.safedriving.service.impl;

import com.safedriving.dto.request.VehicleTypeRequest;
import com.safedriving.dto.response.VehicleTypeResponse;
import com.safedriving.entity.VehicleType;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.VehicleTypeRepository;
import com.safedriving.service.VehicleTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleTypeServiceImpl implements VehicleTypeService {

    private final VehicleTypeRepository vehicleTypeRepository;

    @Override
    public List<VehicleTypeResponse> getAll() {
        return vehicleTypeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VehicleTypeResponse getById(Integer id) {
        VehicleType entity = vehicleTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại xe có ID: " + id));
        return mapToResponse(entity);
    }

    @Override
    @Transactional
    public VehicleTypeResponse create(VehicleTypeRequest request) {
        if (vehicleTypeRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Mã loại xe đã tồn tại: " + request.getCode());
        }
        VehicleType entity = VehicleType.builder()
                .code(request.getCode())
                .name(request.getName())
                .capacity(request.getCapacity())
                .build();
        return mapToResponse(vehicleTypeRepository.save(entity));
    }

    @Override
    @Transactional
    public VehicleTypeResponse update(Integer id, VehicleTypeRequest request) {
        VehicleType entity = vehicleTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại xe có ID: " + id));
        if (!entity.getCode().equals(request.getCode()) && vehicleTypeRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Mã loại xe đã tồn tại: " + request.getCode());
        }
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setCapacity(request.getCapacity());
        return mapToResponse(vehicleTypeRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!vehicleTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy loại xe có ID: " + id);
        }
        vehicleTypeRepository.deleteById(id);
    }

    private VehicleTypeResponse mapToResponse(VehicleType entity) {
        return VehicleTypeResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .capacity(entity.getCapacity())
                .build();
    }
}