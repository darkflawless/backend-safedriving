package com.safedriving.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safedriving.dto.request.RouteRequest;
import com.safedriving.dto.response.RouteResponse;
import com.safedriving.security.JwtAuthenticationEntryPoint;
import com.safedriving.security.JwtAuthenticationFilter;
import com.safedriving.security.JwtTokenProvider;
import com.safedriving.service.RouteService;
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

@WebMvcTest(controllers = RouteController.class)
@AutoConfigureMockMvc(addFilters = false)
class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RouteService routeService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /routes - Lấy danh sách tuyến đường thành công")
    void getAllRoutes_Success() throws Exception {
        RouteResponse response = RouteResponse.builder()
                .id("route-1")
                .code("HN-HP-01")
                .routeName("Hà Nội - Hải Phòng")
                .build();

        when(routeService.getAllRoutes()).thenReturn(List.of(response));

        mockMvc.perform(get("/routes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("route-1"))
                .andExpect(jsonPath("$.data[0].code").value("HN-HP-01"));
    }

    @Test
    @DisplayName("GET /routes/{id} - Lấy chi tiết tuyến đường thành công")
    void getRouteById_Success() throws Exception {
        RouteResponse response = RouteResponse.builder()
                .id("route-1")
                .code("HN-HP-01")
                .routeName("Hà Nội - Hải Phòng")
                .build();

        when(routeService.getRouteById("route-1")).thenReturn(response);

        mockMvc.perform(get("/routes/route-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("route-1"));
    }

    @Test
    @DisplayName("POST /routes - Tạo mới tuyến đường thành công")
    void createRoute_Success() throws Exception {
        RouteRequest request = RouteRequest.builder()
                .code("HN-HP-01")
                .routeName("Hà Nội - Hải Phòng")
                .distanceKm(105.0)
                .standardDurationMin(90)
                .build();

        RouteResponse response = RouteResponse.builder()
                .id("route-1")
                .code("HN-HP-01")
                .build();

        when(routeService.createRoute(any(RouteRequest.class))).thenReturn(response);

        mockMvc.perform(post("/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("route-1"));
    }

    @Test
    @DisplayName("POST /routes - Báo lỗi khi code hoặc routeName bị trống")
    void createRoute_ValidationError() throws Exception {
        RouteRequest request = RouteRequest.builder()
                .code("")
                .routeName("")
                .build();

        mockMvc.perform(post("/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("PUT /routes/{id} - Cập nhật tuyến đường thành công")
    void updateRoute_Success() throws Exception {
        RouteRequest request = RouteRequest.builder()
                .code("HN-HP-01")
                .routeName("Hà Nội - Hải Phòng Mới")
                .distanceKm(110.0)
                .standardDurationMin(95)
                .build();

        RouteResponse response = RouteResponse.builder()
                .id("route-1")
                .routeName("Hà Nội - Hải Phòng Mới")
                .build();

        when(routeService.updateRoute(eq("route-1"), any(RouteRequest.class))).thenReturn(response);

        mockMvc.perform(put("/routes/route-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.routeName").value("Hà Nội - Hải Phòng Mới"));
    }

    @Test
    @DisplayName("DELETE /routes/{id} - Xóa tuyến đường thành công")
    void deleteRoute_Success() throws Exception {
        doNothing().when(routeService).deleteRoute("route-1");

        mockMvc.perform(delete("/routes/route-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xóa tuyến đường thành công"));
    }
}
