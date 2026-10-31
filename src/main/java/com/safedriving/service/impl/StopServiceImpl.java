package com.safedriving.service.impl;

import com.safedriving.dto.request.StopRequest;
import com.safedriving.dto.response.AddressResponse;
import com.safedriving.dto.response.StopResponse;
import com.safedriving.entity.Address;
import com.safedriving.entity.Stop;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.AddressRepository;
import com.safedriving.repository.StopRepository;
import com.safedriving.service.StopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StopServiceImpl implements StopService {

    private final StopRepository stopRepository;
    private final AddressRepository addressRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StopResponse> getAllStops() {
        log.info("Lấy danh sách tất cả điểm dừng");
        return stopRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StopResponse getStopById(String id) {
        log.info("Tra cứu điểm dừng với ID: {}", id);
        Stop stop = findStopOrThrow(id);
        return toResponse(stop);
    }

    @Override
    @Transactional
    public StopResponse createStop(StopRequest request) {
        log.info("Tạo điểm dừng mới: {}", request.getNameStop());
        Address address = addressRepository.findById(request.getAddressId().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ với ID: " + request.getAddressId()));

        Stop stop = Stop.builder()
                .nameStop(request.getNameStop().trim())
                .address(address)
                .type(request.getType())
                .build();

        Stop saved = stopRepository.save(stop);
        log.info("Tạo điểm dừng thành công với ID: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public StopResponse updateStop(String id, StopRequest request) {
        log.info("Cập nhật điểm dừng với ID: {}", id);
        Stop stop = findStopOrThrow(id);

        if (!stop.getAddress().getId().equals(request.getAddressId().trim())) {
            Address address = addressRepository.findById(request.getAddressId().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ với ID: " + request.getAddressId()));
            stop.setAddress(address);
        }

        stop.setNameStop(request.getNameStop().trim());
        stop.setType(request.getType());

        Stop updated = stopRepository.save(stop);
        log.info("Cập nhật điểm dừng thành công cho ID: {}", updated.getId());
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteStop(String id) {
        log.info("Xóa điểm dừng với ID: {}", id);
        Stop stop = findStopOrThrow(id);
        stopRepository.delete(stop);
        log.info("Đã xóa điểm dừng với ID: {}", id);
    }

    private Stop findStopOrThrow(String id) {
        return stopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy điểm dừng với ID: " + id));
    }

    private StopResponse toResponse(Stop stop) {
        Address address = stop.getAddress();
        AddressResponse addressResponse = null;
        if (address != null) {
            addressResponse = AddressResponse.builder()
                    .id(address.getId())
                    .exactAddress(address.getExactAddress())
                    .commune(address.getCommune())
                    .province(address.getProvince())
                    .lat(address.getLat())
                    .lng(address.getLng())
                    .build();
        }

        return StopResponse.builder()
                .id(stop.getId())
                .nameStop(stop.getNameStop())
                .address(addressResponse)
                .type(stop.getType())
                .build();
    }
}
