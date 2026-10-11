package com.safedriving.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safedriving.dto.request.AddressRequest;
import com.safedriving.dto.response.AddressResponse;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.security.JwtAuthenticationEntryPoint;
import com.safedriving.security.JwtAuthenticationFilter;
import com.safedriving.security.JwtTokenProvider;
import com.safedriving.service.AddressService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AddressController.class)
@AutoConfigureMockMvc(addFilters = false)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AddressService addressService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /addresses - Lấy danh sách địa chỉ thành công")
    void getAllAddresses_Success() throws Exception {
        AddressResponse response = AddressResponse.builder()
                .id("addr-uuid-1")
                .exactAddress("123 Phố Huế")
                .commune("Phường Hàng Bài")
                .province("Hà Nội")
                .build();

        when(addressService.getAllAddresses()).thenReturn(List.of(response));

        mockMvc.perform(get("/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("addr-uuid-1"))
                .andExpect(jsonPath("$.data[0].exactAddress").value("123 Phố Huế"));
    }

    @Test
    @DisplayName("GET /addresses/{id} - Lấy chi tiết địa chỉ thành công")
    void getAddressById_Success() throws Exception {
        AddressResponse response = AddressResponse.builder()
                .id("addr-uuid-1")
                .exactAddress("123 Phố Huế")
                .build();

        when(addressService.getAddressById("addr-uuid-1")).thenReturn(response);

        mockMvc.perform(get("/addresses/addr-uuid-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("addr-uuid-1"));
    }

    @Test
    @DisplayName("GET /addresses/{id} - Trả về 404 khi không tìm thấy địa chỉ")
    void getAddressById_NotFound() throws Exception {
        when(addressService.getAddressById("not-found"))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy địa chỉ với ID: not-found"));

        mockMvc.perform(get("/addresses/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /addresses - Tạo địa chỉ thành công")
    void createAddress_Success() throws Exception {
        AddressRequest request = AddressRequest.builder()
                .exactAddress("123 Phố Huế")
                .commune("Phường Hàng Bài")
                .province("Hà Nội")
                .lat(new BigDecimal("21.028511"))
                .lng(new BigDecimal("105.854444"))
                .build();

        AddressResponse response = AddressResponse.builder()
                .id("addr-uuid-1")
                .exactAddress("123 Phố Huế")
                .build();

        when(addressService.createAddress(any(AddressRequest.class))).thenReturn(response);

        mockMvc.perform(post("/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("addr-uuid-1"));
    }

    @Test
    @DisplayName("POST /addresses - Thất bại khi exactAddress bị trống (Validation error)")
    void createAddress_ValidationError() throws Exception {
        AddressRequest request = AddressRequest.builder()
                .exactAddress("")
                .build();

        mockMvc.perform(post("/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("PUT /addresses/{id} - Cập nhật địa chỉ thành công")
    void updateAddress_Success() throws Exception {
        AddressRequest request = AddressRequest.builder()
                .exactAddress("123 Phố Huế Mới")
                .build();

        AddressResponse response = AddressResponse.builder()
                .id("addr-uuid-1")
                .exactAddress("123 Phố Huế Mới")
                .build();

        when(addressService.updateAddress(eq("addr-uuid-1"), any(AddressRequest.class))).thenReturn(response);

        mockMvc.perform(put("/addresses/addr-uuid-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.exactAddress").value("123 Phố Huế Mới"));
    }

    @Test
    @DisplayName("DELETE /addresses/{id} - Xóa địa chỉ thành công")
    void deleteAddress_Success() throws Exception {
        doNothing().when(addressService).deleteAddress("addr-uuid-1");

        mockMvc.perform(delete("/addresses/addr-uuid-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xóa địa chỉ thành công"));
    }
}
