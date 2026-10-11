package com.safedriving.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safedriving.dto.request.ViolationRequest;
import com.safedriving.dto.response.ViolationResponse;
import com.safedriving.entity.enums.ViolationType;
import com.safedriving.security.JwtAuthenticationEntryPoint;
import com.safedriving.security.JwtAuthenticationFilter;
import com.safedriving.security.JwtTokenProvider;
import com.safedriving.service.ViolationService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ViolationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ViolationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ViolationService violationService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /violations - Lấy danh sách thành công")
    void getAllViolations_Success() throws Exception {
        ViolationResponse response = ViolationResponse.builder()
                .id("vio-1")
                .driverId("driver-1")
                .type(ViolationType.SPEEDING)
                .penalty(new BigDecimal("5000000"))
                .build();

        when(violationService.getAllViolations(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/violations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("vio-1"))
                .andExpect(jsonPath("$.data[0].type").value("SPEEDING"));
    }

    @Test
    @DisplayName("GET /violations/{id} - Lấy chi tiết thành công")
    void getViolationById_Success() throws Exception {
        ViolationResponse response = ViolationResponse.builder()
                .id("vio-1")
                .type(ViolationType.SPEEDING)
                .build();

        when(violationService.getViolationById("vio-1")).thenReturn(response);

        mockMvc.perform(get("/violations/vio-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("vio-1"));
    }

    @Test
    @DisplayName("POST /violations - Tạo biên bản thành công")
    void createViolation_Success() throws Exception {
        ViolationRequest request = ViolationRequest.builder()
                .driverId("driver-1")
                .incidentId("incident-1")
                .accountId("acc-1")
                .type(ViolationType.SPEEDING)
                .penalty(new BigDecimal("2000000"))
                .build();

        ViolationResponse response = ViolationResponse.builder()
                .id("vio-1")
                .driverId("driver-1")
                .type(ViolationType.SPEEDING)
                .build();

        when(violationService.createViolation(any(ViolationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/violations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("vio-1"));
    }

    @Test
    @DisplayName("PUT /violations/{id} - Cập nhật biên bản thành công")
    void updateViolation_Success() throws Exception {
        ViolationRequest request = ViolationRequest.builder()
                .driverId("driver-1")
                .incidentId("incident-1")
                .accountId("acc-1")
                .type(ViolationType.RED_LIGHT)
                .build();

        ViolationResponse response = ViolationResponse.builder()
                .id("vio-1")
                .type(ViolationType.RED_LIGHT)
                .build();

        when(violationService.updateViolation(eq("vio-1"), any(ViolationRequest.class))).thenReturn(response);

        mockMvc.perform(put("/violations/vio-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("RED_LIGHT"));
    }

    @Test
    @DisplayName("DELETE /violations/{id} - Xóa biên bản thành công")
    void deleteViolation_Success() throws Exception {
        doNothing().when(violationService).deleteViolation("vio-1");

        mockMvc.perform(delete("/violations/vio-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xóa biên bản vi phạm thành công"));
    }
}
