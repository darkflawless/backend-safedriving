package com.safedriving.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safedriving.dto.request.TripProgressRequest;
import com.safedriving.dto.response.TripProgressResponse;
import com.safedriving.security.JwtAuthenticationEntryPoint;
import com.safedriving.security.JwtAuthenticationFilter;
import com.safedriving.security.JwtTokenProvider;
import com.safedriving.service.TripProgressService;
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

@WebMvcTest(controllers = TripProgressController.class)
@AutoConfigureMockMvc(addFilters = false)
class TripProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TripProgressService tripProgressService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /trip-progress - Lấy danh sách thành công")
    void getAllTripProgress_Success() throws Exception {
        TripProgressResponse response = TripProgressResponse.builder()
                .id("tp-1")
                .tripId("trip-1")
                .order(1)
                .build();

        when(tripProgressService.getAllTripProgress(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/trip-progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("tp-1"));
    }

    @Test
    @DisplayName("GET /trip-progress/{id} - Lấy chi tiết thành công")
    void getTripProgressById_Success() throws Exception {
        TripProgressResponse response = TripProgressResponse.builder()
                .id("tp-1")
                .tripId("trip-1")
                .build();

        when(tripProgressService.getTripProgressById("tp-1")).thenReturn(response);

        mockMvc.perform(get("/trip-progress/tp-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("tp-1"));
    }

    @Test
    @DisplayName("POST /trip-progress - Ghi nhận tiến độ thành công")
    void createTripProgress_Success() throws Exception {
        TripProgressRequest request = TripProgressRequest.builder()
                .tripId("trip-1")
                .order(1)
                .build();

        TripProgressResponse response = TripProgressResponse.builder()
                .id("tp-1")
                .tripId("trip-1")
                .order(1)
                .build();

        when(tripProgressService.createTripProgress(any(TripProgressRequest.class))).thenReturn(response);

        mockMvc.perform(post("/trip-progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("tp-1"));
    }

    @Test
    @DisplayName("PUT /trip-progress/{id} - Cập nhật tiến độ thành công")
    void updateTripProgress_Success() throws Exception {
        TripProgressRequest request = TripProgressRequest.builder()
                .tripId("trip-1")
                .order(2)
                .build();

        TripProgressResponse response = TripProgressResponse.builder()
                .id("tp-1")
                .order(2)
                .build();

        when(tripProgressService.updateTripProgress(eq("tp-1"), any(TripProgressRequest.class))).thenReturn(response);

        mockMvc.perform(put("/trip-progress/tp-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.order").value(2));
    }

    @Test
    @DisplayName("DELETE /trip-progress/{id} - Xóa tiến độ thành công")
    void deleteTripProgress_Success() throws Exception {
        doNothing().when(tripProgressService).deleteTripProgress("tp-1");

        mockMvc.perform(delete("/trip-progress/tp-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xóa tiến độ chuyến đi thành công"));
    }
}
