package com.safedriving.controller;

import com.safedriving.dto.response.AccountResponse;
import com.safedriving.entity.enums.AccountRole;
import com.safedriving.entity.enums.AccountStatus;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.security.JwtAuthenticationEntryPoint;
import com.safedriving.security.JwtAuthenticationFilter;
import com.safedriving.security.JwtTokenProvider;
import com.safedriving.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /accounts - Lấy toàn bộ danh sách tài khoản thành công")
    void getAllAccounts_Success() throws Exception {
        AccountResponse response = AccountResponse.builder()
                .id("acc-1")
                .username("admin")
                .role(AccountRole.ADMIN)
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountService.getAllAccounts()).thenReturn(List.of(response));

        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("acc-1"))
                .andExpect(jsonPath("$.data[0].username").value("admin"));
    }

    @Test
    @DisplayName("GET /accounts/{id} - Lấy tài khoản theo ID thành công")
    void getAccountById_Success() throws Exception {
        AccountResponse response = AccountResponse.builder()
                .id("acc-1")
                .username("admin")
                .build();

        when(accountService.getAccountById("acc-1")).thenReturn(response);

        mockMvc.perform(get("/accounts/acc-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("acc-1"));
    }

    @Test
    @DisplayName("PATCH /accounts/{id}/status - Cập nhật trạng thái thành công")
    void updateAccountStatus_Success() throws Exception {
        AccountResponse response = AccountResponse.builder()
                .id("acc-1")
                .status(AccountStatus.LOCKED)
                .build();

        when(accountService.updateAccountStatus(eq("acc-1"), eq(AccountStatus.LOCKED))).thenReturn(response);

        mockMvc.perform(patch("/accounts/acc-1/status")
                        .param("status", "LOCKED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("LOCKED"));
    }

    @Test
    @DisplayName("PATCH /accounts/{id}/role - Cập nhật quyền thành công")
    void updateAccountRole_Success() throws Exception {
        AccountResponse response = AccountResponse.builder()
                .id("acc-1")
                .role(AccountRole.MANAGER)
                .build();

        when(accountService.updateAccountRole(eq("acc-1"), eq(AccountRole.MANAGER))).thenReturn(response);

        mockMvc.perform(patch("/accounts/acc-1/role")
                        .param("role", "MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("MANAGER"));
    }

    @Test
    @DisplayName("DELETE /accounts/{id} - Xóa mềm tài khoản thành công")
    void deleteAccount_Success() throws Exception {
        doNothing().when(accountService).deleteAccount("acc-1");

        mockMvc.perform(delete("/accounts/acc-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xóa tài khoản thành công"));
    }
}
