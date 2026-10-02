package com.safedriving.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safedriving.dto.request.IdReference;
import com.safedriving.dto.request.StaffRequest;
import com.safedriving.dto.response.StaffAccountInfo;
import com.safedriving.dto.response.StaffResponse;
import com.safedriving.entity.enums.AccountRole;
import com.safedriving.entity.enums.AccountStatus;
import com.safedriving.entity.enums.Gender;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.security.JwtAuthenticationEntryPoint;
import com.safedriving.security.JwtAuthenticationFilter;
import com.safedriving.security.JwtTokenProvider;
import com.safedriving.service.StaffService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StaffController.class)
@AutoConfigureMockMvc(addFilters = false)
class StaffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StaffService staffService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /staff - Lấy toàn bộ danh sách nhân viên thành công")
    void getAllStaff_Success() throws Exception {
        StaffResponse response = StaffResponse.builder()
                .id("staff-1")
                .firstName("Van A")
                .lastName("Nguyen")
                .fullName("Van A Nguyen")
                .dateOfBirth(LocalDate.of(1995, 5, 20))
                .gender(Gender.MALE)
                .email("vana@example.com")
                .phone("0901234567")
                .build();

        when(staffService.getAllStaff()).thenReturn(List.of(response));

        mockMvc.perform(get("/staff"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("staff-1"))
                .andExpect(jsonPath("$.data[0].fullName").value("Van A Nguyen"));
    }

    @Test
    @DisplayName("GET /staff/{id} - Lấy chi tiết nhân viên theo ID thành công")
    void getStaffById_Success() throws Exception {
        StaffResponse response = StaffResponse.builder()
                .id("staff-1")
                .firstName("Van A")
                .lastName("Nguyen")
                .fullName("Van A Nguyen")
                .exactAddress("123 Phố Huế")
                .commune("Hàng Bài")
                .province("Hà Nội")
                .account(StaffAccountInfo.builder().id("acc-1").username("vana").role(AccountRole.ADMIN).status(AccountStatus.ACTIVE).build())
                .build();

        when(staffService.getStaffById("staff-1")).thenReturn(response);

        mockMvc.perform(get("/staff/staff-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("staff-1"))
                .andExpect(jsonPath("$.data.exactAddress").value("123 Phố Huế"))
                .andExpect(jsonPath("$.data.commune").value("Hàng Bài"))
                .andExpect(jsonPath("$.data.province").value("Hà Nội"))
                .andExpect(jsonPath("$.data.account.username").value("vana"));
    }

    @Test
    @DisplayName("GET /staff/{id} - Trả về 404 khi không tìm thấy hồ sơ nhân sự")
    void getStaffById_NotFound() throws Exception {
        when(staffService.getStaffById("not-found"))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy hồ sơ nhân sự với ID: not-found"));

        mockMvc.perform(get("/staff/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /staff - Tạo mới hồ sơ nhân sự thành công")
    void createStaff_Success() throws Exception {
        StaffRequest request = StaffRequest.builder()
                .firstName("Van A")
                .lastName("Nguyen")
                .dateOfBirth(LocalDate.of(1995, 5, 20))
                .gender(Gender.MALE)
                .email("vana@example.com")
                .phone("0901234567")
                .exactAddress("123 Phố Huế")
                .commune("Hàng Bài")
                .province("Hà Nội")
                .account(IdReference.builder().id("acc-1").build())
                .build();

        StaffResponse response = StaffResponse.builder()
                .id("staff-1")
                .firstName("Van A")
                .lastName("Nguyen")
                .fullName("Van A Nguyen")
                .dateOfBirth(LocalDate.of(1995, 5, 20))
                .gender(Gender.MALE)
                .email("vana@example.com")
                .phone("0901234567")
                .build();

        when(staffService.createStaff(any(StaffRequest.class))).thenReturn(response);

        mockMvc.perform(post("/staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("staff-1"))
                .andExpect(jsonPath("$.data.fullName").value("Van A Nguyen"));
    }

    @Test
    @DisplayName("POST /staff - Thất bại do thiếu trường bắt buộc (firstName, lastName, phone)")
    void createStaff_ValidationError() throws Exception {
        StaffRequest request = StaffRequest.builder()
                .firstName("")
                .lastName("")
                .phone("")
                .build();

        mockMvc.perform(post("/staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("PUT /staff/{id} - Cập nhật thông tin nhân viên thành công")
    void updateStaff_Success() throws Exception {
        StaffRequest request = StaffRequest.builder()
                .firstName("Van A Mới")
                .lastName("Nguyen")
                .dateOfBirth(LocalDate.of(1995, 5, 20))
                .phone("0909999999")
                .build();

        StaffResponse response = StaffResponse.builder()
                .id("staff-1")
                .firstName("Van A Mới")
                .lastName("Nguyen")
                .fullName("Van A Mới Nguyen")
                .phone("0909999999")
                .build();

        when(staffService.updateStaff(eq("staff-1"), any(StaffRequest.class))).thenReturn(response);

        mockMvc.perform(put("/staff/staff-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Van A Mới Nguyen"));
    }

    @Test
    @DisplayName("DELETE /staff/{id} - Xóa hồ sơ nhân sự thành công")
    void deleteStaff_Success() throws Exception {
        doNothing().when(staffService).deleteStaff("staff-1");

        mockMvc.perform(delete("/staff/staff-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xóa hồ sơ nhân sự thành công"));
    }

    @Test
    @DisplayName("DELETE /staff/{id} - Thất bại khi nhân viên đang phân công làm tài xế")
    void deleteStaff_BoundToDriver() throws Exception {
        doThrow(new BadRequestException("Không thể xóa nhân viên vì nhân viên này đang được phân công làm tài xế (Driver)"))
                .when(staffService).deleteStaff("staff-driver");

        mockMvc.perform(delete("/staff/staff-driver"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"));
    }
}
