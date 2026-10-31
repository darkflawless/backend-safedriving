package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.dto.request.AddressRequest;
import com.safedriving.dto.response.AddressResponse;
import com.safedriving.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
@Tag(name = "Addresses", description = "APIs Quản lý địa chỉ hành chính và tọa độ GPS")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "Xem danh sách tất cả địa chỉ", description = "Lấy toàn bộ danh sách địa chỉ trong hệ thống.")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAllAddresses() {
        List<AddressResponse> responses = addressService.getAllAddresses();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách địa chỉ thành công", responses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Tra cứu chi tiết một địa chỉ", description = "Xem thông tin chi tiết một địa chỉ theo ID.")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddressById(@PathVariable String id) {
        AddressResponse response = addressService.getAddressById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin địa chỉ thành công", response));
    }

    @PostMapping
    @Operation(summary = "Thêm mới địa chỉ", description = "Tạo mới địa chỉ hành chính kèm tọa độ GPS.")
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(@Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.createAddress(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm mới địa chỉ thành công", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin địa chỉ", description = "Cập nhật dữ liệu địa chỉ đã có trong hệ thống.")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable String id,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.updateAddress(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật địa chỉ thành công", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa một bản ghi địa chỉ", description = "Xóa bản ghi địa chỉ theo ID.")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable String id) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa địa chỉ thành công"));
    }
}
