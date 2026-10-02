package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.dto.response.AccountResponse;
import com.safedriving.entity.enums.AccountRole;
import com.safedriving.entity.enums.AccountStatus;
import com.safedriving.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Account", description = "Nhóm Quản trị Tài khoản & Phân quyền (Account Management)")
@PreAuthorize("hasRole('ADMIN')")
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    @Operation(summary = "Get All Accounts", description = "Lấy danh sách tất cả tài khoản trong hệ thống.")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts() {
        List<AccountResponse> responses = accountService.getAllAccounts();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tài khoản thành công", responses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Account By Id", description = "Xem chi tiết thông tin của một tài khoản theo ID.")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(@PathVariable String id) {
        AccountResponse response = accountService.getAccountById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin tài khoản thành công", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update Account Status", description = "Khóa (LOCKED), mở khóa hoặc kích hoạt (ACTIVE) tài khoản.")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccountStatus(
            @PathVariable String id,
            @RequestParam AccountStatus status) {
        AccountResponse response = accountService.updateAccountStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái tài khoản thành công", response));
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Update Account Role", description = "Cập nhật vai trò phân quyền (ADMIN, MANAGER, DRIVER).")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccountRole(
            @PathVariable String id,
            @RequestParam AccountRole role) {
        AccountResponse response = accountService.updateAccountRole(id, role);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật quyền tài khoản thành công", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Account", description = "Xóa mềm tài khoản người dùng (Soft Delete).")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable String id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa tài khoản thành công"));
    }
}
