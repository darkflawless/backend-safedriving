package com.safedriving.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safedriving.dto.request.DeviceRequest;
import com.safedriving.dto.response.DeviceResponse;
import com.safedriving.entity.enums.DeviceStatus;
import com.safedriving.security.JwtAuthenticationEntryPoint;
import com.safedriving.security.JwtAuthenticationFilter;
import com.safedriving.security.JwtTokenProvider;
import com.safedriving.service.DeviceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DeviceController.class)
@AutoConfigureMockMvc(addFilters = false)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeviceService deviceService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /devices - Success")
    void getAllDevices_Success() throws Exception {
        DeviceResponse response = DeviceResponse.builder()
                .id("device-1")
                .serialNumber("SN123")
                .build();

        when(deviceService.getAllDevices()).thenReturn(List.of(response));

        mockMvc.perform(get("/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].serialNumber").value("SN123"));
    }

    @Test
    @DisplayName("GET /devices/{id} - Success")
    void getDeviceById_Success() throws Exception {
        DeviceResponse response = DeviceResponse.builder()
                .id("device-1")
                .serialNumber("SN123")
                .build();

        when(deviceService.getDeviceById("device-1")).thenReturn(response);

        mockMvc.perform(get("/devices/device-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.serialNumber").value("SN123"));
    }

    @Test
    @DisplayName("POST /devices - Success")
    void createDevice_Success() throws Exception {
        DeviceRequest request = DeviceRequest.builder()
                .serialNumber("SN123")
                .macAddress("MAC123")
                .deviceStatus(DeviceStatus.ACTIVE)
                .build();

        DeviceResponse response = DeviceResponse.builder()
                .id("device-1")
                .serialNumber("SN123")
                .build();

        when(deviceService.createDevice(any(DeviceRequest.class))).thenReturn(response);

        mockMvc.perform(post("/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.serialNumber").value("SN123"));
    }

    @Test
    @DisplayName("PUT /devices/{id} - Success")
    void updateDevice_Success() throws Exception {
        DeviceRequest request = DeviceRequest.builder()
                .deviceStatus(DeviceStatus.STOPPED)
                .build();

        DeviceResponse response = DeviceResponse.builder()
                .id("device-1")
                .deviceStatus(DeviceStatus.STOPPED)
                .build();

        when(deviceService.updateDevice(eq("device-1"), any(DeviceRequest.class))).thenReturn(response);

        mockMvc.perform(put("/devices/device-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.deviceStatus").value("STOPPED"));
    }

    @Test
    @DisplayName("DELETE /devices/{id} - Success")
    void deleteDevice_Success() throws Exception {
        doNothing().when(deviceService).deleteDevice("device-1");

        mockMvc.perform(delete("/devices/device-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
