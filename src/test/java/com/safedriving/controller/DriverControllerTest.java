package com.safedriving.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safedriving.dto.request.DriverRequest;
import com.safedriving.dto.request.IdReference;
import com.safedriving.dto.response.DriverResponse;
import com.safedriving.dto.response.DriverVehicleInfo;
import com.safedriving.dto.response.StaffResponse;
import com.safedriving.entity.enums.Gender;
import com.safedriving.entity.enums.VehicleStatus;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.security.JwtAuthenticationEntryPoint;
import com.safedriving.security.JwtAuthenticationFilter;
import com.safedriving.security.JwtTokenProvider;
import com.safedriving.service.DriverService;
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

@WebMvcTest(controllers = DriverController.class)
@AutoConfigureMockMvc(addFilters = false)
class DriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DriverService driverService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /drivers - Lấy danh sách tài xế thành công")
    void getAllDrivers_Success() throws Exception {
        DriverResponse response = DriverResponse.builder()
                .id("driver-1")
                .hireDate(LocalDate.of(2022, 5, 10))
                .isActive(true)
                .staff(StaffResponse.builder().id("staff-1").firstName("Van A").lastName("Nguyen").fullName("Van A Nguyen").gender(Gender.MALE).build())
                .vehicle(DriverVehicleInfo.builder().id("veh-1").plateNumber("29B-12345").status(VehicleStatus.AVAILABLE).build())
                .build();

        when(driverService.getAllDrivers()).thenReturn(List.of(response));

        mockMvc.perform(get("/drivers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("driver-1"))
                .andExpect(jsonPath("$.data[0].staff.fullName").value("Van A Nguyen"))
                .andExpect(jsonPath("$.data[0].vehicle.plateNumber").value("29B-12345"));
    }

    @Test
    @DisplayName("GET /drivers/{id} - Tra cứu hồ sơ chi tiết một tài xế thành công")
    void getDriverById_Success() throws Exception {
        DriverResponse response = DriverResponse.builder()
                .id("driver-1")
                .hireDate(LocalDate.of(2022, 5, 10))
                .isActive(true)
                .staff(StaffResponse.builder().id("staff-1").firstName("Van A").lastName("Nguyen").fullName("Van A Nguyen").build())
                .build();

        when(driverService.getDriverById("driver-1")).thenReturn(response);

        mockMvc.perform(get("/drivers/driver-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("driver-1"))
                .andExpect(jsonPath("$.data.staff.fullName").value("Van A Nguyen"));
    }

    @Test
    @DisplayName("GET /drivers/{id} - Trả về 404 khi không tìm thấy tài xế")
    void getDriverById_NotFound() throws Exception {
        when(driverService.getDriverById("driver-unknown"))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy hồ sơ tài xế với ID: driver-unknown"));

        mockMvc.perform(get("/drivers/driver-unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /drivers - Tạo hồ sơ tài xế mới thành công")
    void createDriver_Success() throws Exception {
        DriverRequest request = DriverRequest.builder()
                .hireDate(LocalDate.of(2023, 1, 15))
                .urlImage("https://example.com/avatar.jpg")
                .isActive(true)
                .staff(IdReference.builder().id("staff-1").build())
                .build();

        DriverResponse response = DriverResponse.builder()
                .id("driver-2")
                .hireDate(LocalDate.of(2023, 1, 15))
                .isActive(true)
                .build();

        when(driverService.createDriver(any(DriverRequest.class))).thenReturn(response);

        mockMvc.perform(post("/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("driver-2"));
    }

    @Test
    @DisplayName("POST /drivers - Thất bại khi thiếu hireDate")
    void createDriver_MissingHireDate_ValidationError() throws Exception {
        DriverRequest request = DriverRequest.builder()
                .staff(IdReference.builder().id("staff-1").build())
                .build();

        mockMvc.perform(post("/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("PUT /drivers/{id} - Cập nhật thông tin hồ sơ tài xế thành công")
    void updateDriver_Success() throws Exception {
        DriverRequest request = DriverRequest.builder()
                .hireDate(LocalDate.of(2022, 5, 10))
                .isActive(false)
                .build();

        DriverResponse response = DriverResponse.builder()
                .id("driver-1")
                .hireDate(LocalDate.of(2022, 5, 10))
                .isActive(false)
                .build();

        when(driverService.updateDriver(eq("driver-1"), any(DriverRequest.class))).thenReturn(response);

        mockMvc.perform(put("/drivers/driver-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isActive").value(false));
    }

    @Test
    @DisplayName("DELETE /drivers/{id} - Xóa thông tin tài xế thành công")
    void deleteDriver_Success() throws Exception {
        doNothing().when(driverService).deleteDriver("driver-1");

        mockMvc.perform(delete("/drivers/driver-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xóa thông tin tài xế thành công"));
    }
}
