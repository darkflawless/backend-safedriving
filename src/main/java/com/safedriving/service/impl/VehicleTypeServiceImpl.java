package com.safedriving.service.impl;

import com.safedriving.dto.request.VehicleTypeRequest;
import com.safedriving.dto.response.VehicleTypeResponse;
import com.safedriving.entity.VehicleType;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.VehicleTypeRepository;
import com.safedriving.service.VehicleTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Triển khai nghiệp vụ CRUD danh mục loại phương tiện.
 *
 * @author phanthuy0811
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleTypeServiceImpl implements VehicleTypeService {

    private final VehicleTypeRepository vehicleTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VehicleTypeResponse> getAll() {
        return vehicleTypeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleTypeResponse getById(Short id) {
        VehicleType type = vehicleTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại xe có ID: " + id));
        return mapToResponse(type);
    }

    @Override
    @Transactional
    public VehicleTypeResponse create(VehicleTypeRequest request) {
        String cleanCode = request.getCode().trim().toUpperCase();
        if (vehicleTypeRepository.existsByCode(cleanCode)) {
            throw new BadRequestException("Mã loại xe đã tồn tại: " + cleanCode);
        }

        Short nextId = (short) (vehicleTypeRepository.findMaxId().orElse((short) 0) + 1);

        VehicleType type = VehicleType.builder()
                .id(nextId)
                .code(cleanCode)
                .name(request.getName().trim())
                .capacity(request.getCapacity())
                .build();

        VehicleType saved = vehicleTypeRepository.save(type);
        log.info("✅ Đã tạo mới loại xe: {} - {} (ID: {})", saved.getCode(), saved.getName(), saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public VehicleTypeResponse update(Short id, VehicleTypeRequest request) {
        VehicleType type = vehicleTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại xe có ID: " + id));

        String cleanCode = request.getCode().trim().toUpperCase();
        if (vehicleTypeRepository.existsByCodeAndIdNot(cleanCode, id)) {
            throw new BadRequestException("Mã loại xe đã được sử dụng bởi loại xe khác: " + cleanCode);
        }

        type.setCode(cleanCode);
        type.setName(request.getName().trim());
        type.setCapacity(request.getCapacity());

        VehicleType updated = vehicleTypeRepository.save(type);
        log.info("✅ Đã cập nhật loại xe: {} (ID: {})", updated.getCode(), updated.getId());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Short id) {
        VehicleType type = vehicleTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại xe có ID: " + id));

        vehicleTypeRepository.delete(type);
        log.info("🗑️ Đã xóa loại xe ID: {}", id);
    }

    private VehicleTypeResponse mapToResponse(VehicleType type) {
        return VehicleTypeResponse.builder()
                .id(type.getId())
                .code(type.getCode())
                .name(type.getName())
                .capacity(type.getCapacity())
                .build();
    }
}
