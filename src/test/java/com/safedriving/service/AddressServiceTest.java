package com.safedriving.service;

import com.safedriving.dto.request.AddressRequest;
import com.safedriving.dto.response.AddressResponse;
import com.safedriving.entity.Address;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.AddressRepository;
import com.safedriving.service.impl.AddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    private Address testAddress;

    @BeforeEach
    void setUp() {
        testAddress = Address.builder()
                .id("addr-uuid-1")
                .exactAddress("123 Phố Huế")
                .commune("Phường Hàng Bài")
                .province("Hà Nội")
                .lat(new BigDecimal("21.028511"))
                .lng(new BigDecimal("105.854444"))
                .build();
    }

    @Test
    @DisplayName("getAllAddresses - Thành công trả về danh sách địa chỉ")
    void getAllAddresses_Success() {
        when(addressRepository.findAll()).thenReturn(List.of(testAddress));

        List<AddressResponse> results = addressService.getAllAddresses();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("123 Phố Huế", results.get(0).getExactAddress());
    }

    @Test
    @DisplayName("getAddressById - Thành công khi ID tồn tại")
    void getAddressById_Success() {
        when(addressRepository.findById("addr-uuid-1")).thenReturn(Optional.of(testAddress));

        AddressResponse response = addressService.getAddressById("addr-uuid-1");

        assertNotNull(response);
        assertEquals("addr-uuid-1", response.getId());
        assertEquals("123 Phố Huế", response.getExactAddress());
    }

    @Test
    @DisplayName("getAddressById - Thất bại ném ResourceNotFoundException khi ID không tồn tại")
    void getAddressById_NotFound() {
        when(addressRepository.findById("addr-non-existent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> addressService.getAddressById("addr-non-existent"));
    }

    @Test
    @DisplayName("createAddress - Thành công lưu và trả về AddressResponse")
    void createAddress_Success() {
        AddressRequest request = AddressRequest.builder()
                .exactAddress("456 Giải Phóng")
                .commune("Phương Liệt")
                .province("Hà Nội")
                .lat(new BigDecimal("20.998"))
                .lng(new BigDecimal("105.842"))
                .build();

        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
            Address addr = invocation.getArgument(0);
            addr.setId("addr-uuid-2");
            return addr;
        });

        AddressResponse response = addressService.createAddress(request);

        assertNotNull(response);
        assertEquals("addr-uuid-2", response.getId());
        assertEquals("456 Giải Phóng", response.getExactAddress());
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("updateAddress - Thành công cập nhật địa chỉ")
    void updateAddress_Success() {
        AddressRequest updateReq = AddressRequest.builder()
                .exactAddress("123 Phố Huế Mới")
                .commune("Phường Hàng Bài")
                .province("Hà Nội")
                .lat(new BigDecimal("21.028511"))
                .lng(new BigDecimal("105.854444"))
                .build();

        when(addressRepository.findById("addr-uuid-1")).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

        AddressResponse response = addressService.updateAddress("addr-uuid-1", updateReq);

        assertNotNull(response);
        assertEquals("123 Phố Huế Mới", response.getExactAddress());
        verify(addressRepository).save(testAddress);
    }

    @Test
    @DisplayName("deleteAddress - Thành công xóa địa chỉ")
    void deleteAddress_Success() {
        when(addressRepository.findById("addr-uuid-1")).thenReturn(Optional.of(testAddress));

        addressService.deleteAddress("addr-uuid-1");

        verify(addressRepository).delete(testAddress);
    }
}
