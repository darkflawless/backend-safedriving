package com.safedriving.service;

import com.safedriving.dto.request.AddressRequest;
import com.safedriving.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {

    List<AddressResponse> getAllAddresses();

    AddressResponse getAddressById(String id);

    AddressResponse createAddress(AddressRequest request);

    AddressResponse updateAddress(String id, AddressRequest request);

    void deleteAddress(String id);
}
