package com.safedriving.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safedriving.dto.request.LicenseRequest;
import com.safedriving.dto.response.LicenseResponse;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.security.JwtAuthenticationEntryPoint;
import com.safedriving.security.JwtAuthenticationFilter;
import com.safedriving.security.JwtTokenProvider;
import com.safedriving.service.LicenseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
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

@WebMvcTest(controllers = LicenseController.class)
@AutoConfigureMockMvc(addFilters = false)
class LicenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LicenseService licenseService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /licenses - Success")
    void getAllLicenses_Success() throws Exception {
        LicenseResponse response = LicenseResponse.builder()
                .id("license-1")
                .licenseNo("123456789012")
                .build();

        when(licenseService.getAllLicenses(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/licenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("license-1"));
    }

    @Test
    @DisplayName("GET /licenses/{id} - Success")
    void getLicenseById_Success() throws Exception {
        LicenseResponse response = LicenseResponse.builder()
                .id("license-1")
                .licenseNo("123456789012")
                .build();

        when(licenseService.getLicenseById("license-1")).thenReturn(response);

        mockMvc.perform(get("/licenses/license-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("license-1"));
    }

    @Test
    @DisplayName("POST /licenses - Success")
    void createLicense_Success() throws Exception {
        LicenseRequest request = LicenseRequest.builder()
                .licenseNo("123456789012")
                .issueDate(LocalDate.of(2021, 1, 1))
                .licenseExpiry(LocalDate.of(2031, 1, 1))
                .placeOfIssue("HN")
                .build();

        LicenseResponse response = LicenseResponse.builder()
                .id("license-1")
                .licenseNo("123456789012")
                .build();

        when(licenseService.createLicense(any(LicenseRequest.class))).thenReturn(response);

        mockMvc.perform(post("/licenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("license-1"));
    }

    @Test
    @DisplayName("PUT /licenses/{id} - Success")
    void updateLicense_Success() throws Exception {
        LicenseRequest request = LicenseRequest.builder()
                .placeOfIssue("HCM")
                .build();

        LicenseResponse response = LicenseResponse.builder()
                .id("license-1")
                .placeOfIssue("HCM")
                .build();

        when(licenseService.updateLicense(eq("license-1"), any(LicenseRequest.class))).thenReturn(response);

        mockMvc.perform(put("/licenses/license-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.placeOfIssue").value("HCM"));
    }

    @Test
    @DisplayName("DELETE /licenses/{id} - Success")
    void deleteLicense_Success() throws Exception {
        doNothing().when(licenseService).deleteLicense("license-1");

        mockMvc.perform(delete("/licenses/license-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
