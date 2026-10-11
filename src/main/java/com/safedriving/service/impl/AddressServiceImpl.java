package com.safedriving.service.impl;

import com.safedriving.dto.request.AddressRequest;
import com.safedriving.dto.response.AddressResponse;
import com.safedriving.entity.Address;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.AddressRepository;
import com.safedriving.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getAllAddresses() {
        log.info("Lấy danh sách tất cả địa chỉ");
        return addressRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(String id) {
        log.info("Tra cứu thông tin địa chỉ với ID: {}", id);
        Address address = findAddressOrThrow(id);
        return toResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse createAddress(AddressRequest request) {
        log.info("Tạo địa chỉ mới: {}", request.getExactAddress());
        Address address = Address.builder()
                .exactAddress(request.getExactAddress().trim())
                .commune(request.getCommune() != null ? request.getCommune().trim() : null)
                .province(request.getProvince() != null ? request.getProvince().trim() : null)
                .lat(request.getLat())
                .lng(request.getLng())
                .build();

        Address saved = addressRepository.save(address);
        log.info("Tạo địa chỉ thành công với ID: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(String id, AddressRequest request) {
        log.info("Cập nhật địa chỉ với ID: {}", id);
        Address address = findAddressOrThrow(id);

        address.setExactAddress(request.getExactAddress().trim());
        address.setCommune(request.getCommune() != null ? request.getCommune().trim() : null);
        address.setProvince(request.getProvince() != null ? request.getProvince().trim() : null);
        address.setLat(request.getLat());
        address.setLng(request.getLng());

        Address updated = addressRepository.save(address);
        log.info("Cập nhật địa chỉ thành công cho ID: {}", updated.getId());
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAddress(String id) {
        log.info("Xóa địa chỉ với ID: {}", id);
        Address address = findAddressOrThrow(id);
        addressRepository.delete(address);
        log.info("Đã xóa địa chỉ với ID: {}", id);
    }

    private Address findAddressOrThrow(String id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ với ID: " + id));
    }

    private AddressResponse toResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .exactAddress(address.getExactAddress())
                .commune(address.getCommune())
                .province(address.getProvince())
                .lat(address.getLat())
                .lng(address.getLng())
                .build();
    }
}
