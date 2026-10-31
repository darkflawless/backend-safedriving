package com.safedriving.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safedriving.dto.request.VehicleLogRequest;
import com.safedriving.dto.response.VehicleLogResponse;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.security.JwtAuthenticationEntryPoint;
import com.safedriving.security.JwtAuthenticationFilter;
import com.safedriving.security.JwtTokenProvider;
import com.safedriving.service.VehicleLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

@WebMvcTest(controllers = VehicleLogController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VehicleLogService vehicleLogService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /vehicle-logs - Lấy danh sách log thành công")
    void getAllVehicleLogs_Success() throws Exception {
        VehicleLogResponse response = VehicleLogResponse.builder()
                .id(1L)
                .vehicleId("veh-1")
                .plateNumber("29A-123.45")
                .timeVehicleLog(LocalDateTime.now())
                .lat(new BigDecimal("21.028511"))
                .lng(new BigDecimal("105.854444"))
                .build();

        when(vehicleLogService.getAllVehicleLogs(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/vehicle-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].plateNumber").value("29A-123.45"));
    }

    @Test
    @DisplayName("GET /vehicle-logs/{id} - Lấy chi tiết log thành công")
    void getVehicleLogById_Success() throws Exception {
        VehicleLogResponse response = VehicleLogResponse.builder()
                .id(1L)
                .vehicleId("veh-1")
                .plateNumber("29A-123.45")
                .build();

        when(vehicleLogService.getVehicleLogById(1L)).thenReturn(response);

        mockMvc.perform(get("/vehicle-logs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("POST /vehicle-logs - Tạo log thành công")
    void createVehicleLog_Success() throws Exception {
        VehicleLogRequest request = VehicleLogRequest.builder()
                .vehicleId("veh-1")
                .lat(new BigDecimal("21.028511"))
                .lng(new BigDecimal("105.854444"))
                .build();

        VehicleLogResponse response = VehicleLogResponse.builder()
                .id(1L)
                .vehicleId("veh-1")
                .build();

        when(vehicleLogService.createVehicleLog(any(VehicleLogRequest.class))).thenReturn(response);

        mockMvc.perform(post("/vehicle-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("POST /vehicle-logs - Thất bại khi vehicleId trống (Validation error)")
    void createVehicleLog_ValidationError() throws Exception {
        VehicleLogRequest request = VehicleLogRequest.builder()
                .vehicleId("")
                .build();

        mockMvc.perform(post("/vehicle-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("PUT /vehicle-logs/{id} - Cập nhật log thành công")
    void updateVehicleLog_Success() throws Exception {
        VehicleLogRequest request = VehicleLogRequest.builder()
                .vehicleId("veh-1")
                .lat(new BigDecimal("21.029"))
                .build();

        VehicleLogResponse response = VehicleLogResponse.builder()
                .id(1L)
                .vehicleId("veh-1")
                .lat(new BigDecimal("21.029"))
                .build();

        when(vehicleLogService.updateVehicleLog(eq(1L), any(VehicleLogRequest.class))).thenReturn(response);

        mockMvc.perform(put("/vehicle-logs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("DELETE /vehicle-logs/{id} - Xóa log thành công")
    void deleteVehicleLog_Success() throws Exception {
        doNothing().when(vehicleLogService).deleteVehicleLog(1L);

        mockMvc.perform(delete("/vehicle-logs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xóa nhật ký phương tiện thành công"));
    }
}
