package com.safedriving.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safedriving.dto.request.RouteStopRequest;
import com.safedriving.dto.response.RouteStopResponse;
import com.safedriving.security.JwtAuthenticationEntryPoint;
import com.safedriving.security.JwtAuthenticationFilter;
import com.safedriving.security.JwtTokenProvider;
import com.safedriving.service.RouteStopService;
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

@WebMvcTest(controllers = RouteStopController.class)
@AutoConfigureMockMvc(addFilters = false)
class RouteStopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RouteStopService routeStopService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /route-stops - Lấy danh sách thành công")
    void getAllRouteStops_Success() throws Exception {
        RouteStopResponse response = RouteStopResponse.builder()
                .id("rs-1")
                .routeId("route-1")
                .order(1)
                .build();

        when(routeStopService.getAllRouteStops(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/route-stops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("rs-1"));
    }

    @Test
    @DisplayName("GET /route-stops/{id} - Lấy chi tiết thành công")
    void getRouteStopById_Success() throws Exception {
        RouteStopResponse response = RouteStopResponse.builder()
                .id("rs-1")
                .order(1)
                .build();

        when(routeStopService.getRouteStopById("rs-1")).thenReturn(response);

        mockMvc.perform(get("/route-stops/rs-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("rs-1"));
    }

    @Test
    @DisplayName("POST /route-stops - Tạo thành công")
    void createRouteStop_Success() throws Exception {
        RouteStopRequest request = RouteStopRequest.builder()
                .routeId("route-1")
                .stopId("stop-1")
                .order(1)
                .build();

        RouteStopResponse response = RouteStopResponse.builder()
                .id("rs-1")
                .routeId("route-1")
                .order(1)
                .build();

        when(routeStopService.createRouteStop(any(RouteStopRequest.class))).thenReturn(response);

        mockMvc.perform(post("/route-stops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("rs-1"));
    }

    @Test
    @DisplayName("PUT /route-stops/{id} - Cập nhật thành công")
    void updateRouteStop_Success() throws Exception {
        RouteStopRequest request = RouteStopRequest.builder()
                .routeId("route-1")
                .stopId("stop-1")
                .order(2)
                .build();

        RouteStopResponse response = RouteStopResponse.builder()
                .id("rs-1")
                .order(2)
                .build();

        when(routeStopService.updateRouteStop(eq("rs-1"), any(RouteStopRequest.class))).thenReturn(response);

        mockMvc.perform(put("/route-stops/rs-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.order").value(2));
    }

    @Test
    @DisplayName("DELETE /route-stops/{id} - Xóa thành công")
    void deleteRouteStop_Success() throws Exception {
        doNothing().when(routeStopService).deleteRouteStop("rs-1");

        mockMvc.perform(delete("/route-stops/rs-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xóa điểm dừng khỏi tuyến thành công"));
    }
}
