package com.safedriving.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safedriving.dto.request.AlcoholRecordRequest;
import com.safedriving.dto.response.AlcoholRecordResponse;
import com.safedriving.security.JwtAuthenticationEntryPoint;
import com.safedriving.security.JwtAuthenticationFilter;
import com.safedriving.security.JwtTokenProvider;
import com.safedriving.service.AlcoholRecordService;
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

@WebMvcTest(controllers = AlcoholRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
class AlcoholRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlcoholRecordService alcoholRecordService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /alcohol-records - Success")
    void getAllAlcoholRecords_Success() throws Exception {
        AlcoholRecordResponse response = AlcoholRecordResponse.builder()
                .id(1L)
                .alcoholLevel(new BigDecimal("0.0"))
                .build();

        when(alcoholRecordService.getAllAlcoholRecords(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/alcohol-records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    @DisplayName("GET /alcohol-records/{id} - Success")
    void getAlcoholRecordById_Success() throws Exception {
        AlcoholRecordResponse response = AlcoholRecordResponse.builder()
                .id(1L)
                .alcoholLevel(new BigDecimal("0.0"))
                .build();

        when(alcoholRecordService.getAlcoholRecordById(1L)).thenReturn(response);

        mockMvc.perform(get("/alcohol-records/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("POST /alcohol-records - Success")
    void createAlcoholRecord_Success() throws Exception {
        AlcoholRecordRequest request = AlcoholRecordRequest.builder()
                .driverId("driver-1")
                .alcoholLevel(new BigDecimal("0.0"))
                .build();

        AlcoholRecordResponse response = AlcoholRecordResponse.builder()
                .id(1L)
                .alcoholLevel(new BigDecimal("0.0"))
                .build();

        when(alcoholRecordService.createAlcoholRecord(any(AlcoholRecordRequest.class))).thenReturn(response);

        mockMvc.perform(post("/alcohol-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("PUT /alcohol-records/{id} - Success")
    void updateAlcoholRecord_Success() throws Exception {
        AlcoholRecordRequest request = AlcoholRecordRequest.builder()
                .alcoholLevel(new BigDecimal("0.2"))
                .build();

        AlcoholRecordResponse response = AlcoholRecordResponse.builder()
                .id(1L)
                .alcoholLevel(new BigDecimal("0.2"))
                .build();

        when(alcoholRecordService.updateAlcoholRecord(eq(1L), any(AlcoholRecordRequest.class))).thenReturn(response);

        mockMvc.perform(put("/alcohol-records/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.alcoholLevel").value(0.2));
    }

    @Test
    @DisplayName("DELETE /alcohol-records/{id} - Success")
    void deleteAlcoholRecord_Success() throws Exception {
        doNothing().when(alcoholRecordService).deleteAlcoholRecord(1L);

        mockMvc.perform(delete("/alcohol-records/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
