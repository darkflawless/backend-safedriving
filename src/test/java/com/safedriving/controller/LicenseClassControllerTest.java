package com.safedriving.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safedriving.dto.request.LicenseClassRequest;
import com.safedriving.dto.response.LicenseClassResponse;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.security.JwtAuthenticationEntryPoint;
import com.safedriving.security.JwtAuthenticationFilter;
import com.safedriving.security.JwtTokenProvider;
import com.safedriving.service.LicenseClassService;
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

@WebMvcTest(controllers = LicenseClassController.class)
@AutoConfigureMockMvc(addFilters = false)
class LicenseClassControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LicenseClassService licenseClassService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /license-classes - Success")
    void getAllLicenseClasses_Success() throws Exception {
        LicenseClassResponse response = LicenseClassResponse.builder()
                .id((short) 1)
                .code("B2")
                .name("Hạng B2")
                .capacity(9)
                .build();

        when(licenseClassService.getAllLicenseClasses()).thenReturn(List.of(response));

        mockMvc.perform(get("/license-classes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].code").value("B2"));
    }

    @Test
    @DisplayName("GET /license-classes/{id} - Success")
    void getLicenseClassById_Success() throws Exception {
        LicenseClassResponse response = LicenseClassResponse.builder()
                .id((short) 1)
                .code("B2")
                .build();

        when(licenseClassService.getLicenseClassById((short) 1)).thenReturn(response);

        mockMvc.perform(get("/license-classes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("B2"));
    }

    @Test
    @DisplayName("POST /license-classes - Success")
    void createLicenseClass_Success() throws Exception {
        LicenseClassRequest request = LicenseClassRequest.builder()
                .id((short) 1)
                .code("B2")
                .name("Hạng B2")
                .capacity(9)
                .build();

        LicenseClassResponse response = LicenseClassResponse.builder()
                .id((short) 1)
                .code("B2")
                .build();

        when(licenseClassService.createLicenseClass(any(LicenseClassRequest.class))).thenReturn(response);

        mockMvc.perform(post("/license-classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("B2"));
    }

    @Test
    @DisplayName("PUT /license-classes/{id} - Success")
    void updateLicenseClass_Success() throws Exception {
        LicenseClassRequest request = LicenseClassRequest.builder()
                .code("C")
                .build();

        LicenseClassResponse response = LicenseClassResponse.builder()
                .id((short) 1)
                .code("C")
                .build();

        when(licenseClassService.updateLicenseClass(eq((short) 1), any(LicenseClassRequest.class))).thenReturn(response);

        mockMvc.perform(put("/license-classes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("C"));
    }

    @Test
    @DisplayName("DELETE /license-classes/{id} - Success")
    void deleteLicenseClass_Success() throws Exception {
        doNothing().when(licenseClassService).deleteLicenseClass((short) 1);

        mockMvc.perform(delete("/license-classes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
