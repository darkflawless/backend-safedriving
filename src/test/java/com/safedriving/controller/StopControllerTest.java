package com.safedriving.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safedriving.dto.request.StopRequest;
import com.safedriving.dto.response.AddressResponse;
import com.safedriving.dto.response.StopResponse;
import com.safedriving.entity.enums.StopType;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.security.JwtAuthenticationEntryPoint;
import com.safedriving.security.JwtAuthenticationFilter;
import com.safedriving.security.JwtTokenProvider;
import com.safedriving.service.StopService;
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

@WebMvcTest(controllers = StopController.class)
@AutoConfigureMockMvc(addFilters = false)
class StopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StopService stopService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /stops - Lấy danh sách điểm dừng thành công")
    void getAllStops_Success() throws Exception {
        StopResponse response = StopResponse.builder()
                .id("stop-1")
                .nameStop("Trạm Mỹ Đình")
                .type(StopType.STATION)
                .address(AddressResponse.builder().id("addr-1").exactAddress("Bến xe Mỹ Đình").build())
                .build();

        when(stopService.getAllStops()).thenReturn(List.of(response));

        mockMvc.perform(get("/stops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("stop-1"))
                .andExpect(jsonPath("$.data[0].nameStop").value("Trạm Mỹ Đình"))
                .andExpect(jsonPath("$.data[0].address.id").value("addr-1"));
    }

    @Test
    @DisplayName("GET /stops/{id} - Lấy chi tiết điểm dừng thành công")
    void getStopById_Success() throws Exception {
        StopResponse response = StopResponse.builder()
                .id("stop-1")
                .nameStop("Trạm Mỹ Đình")
                .type(StopType.STATION)
                .build();

        when(stopService.getStopById("stop-1")).thenReturn(response);

        mockMvc.perform(get("/stops/stop-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("stop-1"));
    }

    @Test
    @DisplayName("POST /stops - Tạo điểm dừng thành công")
    void createStop_Success() throws Exception {
        StopRequest request = StopRequest.builder()
                .nameStop("Trạm Mỹ Đình")
                .addressId("addr-1")
                .type(StopType.STATION)
                .build();

        StopResponse response = StopResponse.builder()
                .id("stop-1")
                .nameStop("Trạm Mỹ Đình")
                .type(StopType.STATION)
                .build();

        when(stopService.createStop(any(StopRequest.class))).thenReturn(response);

        mockMvc.perform(post("/stops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("stop-1"));
    }

    @Test
    @DisplayName("POST /stops - Thất bại khi nameStop hoặc addressId trống (Validation error)")
    void createStop_ValidationError() throws Exception {
        StopRequest request = StopRequest.builder()
                .nameStop("")
                .addressId("")
                .build();

        mockMvc.perform(post("/stops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("PUT /stops/{id} - Cập nhật điểm dừng thành công")
    void updateStop_Success() throws Exception {
        StopRequest request = StopRequest.builder()
                .nameStop("Trạm Mỹ Đình Mới")
                .addressId("addr-1")
                .type(StopType.DEPOT)
                .build();

        StopResponse response = StopResponse.builder()
                .id("stop-1")
                .nameStop("Trạm Mỹ Đình Mới")
                .type(StopType.DEPOT)
                .build();

        when(stopService.updateStop(eq("stop-1"), any(StopRequest.class))).thenReturn(response);

        mockMvc.perform(put("/stops/stop-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nameStop").value("Trạm Mỹ Đình Mới"));
    }

    @Test
    @DisplayName("DELETE /stops/{id} - Xóa điểm dừng thành công")
    void deleteStop_Success() throws Exception {
        doNothing().when(stopService).deleteStop("stop-1");

        mockMvc.perform(delete("/stops/stop-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xóa điểm dừng thành công"));
    }
}
