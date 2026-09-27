package com.safedriving.service;

import com.safedriving.dto.response.AccountResponse;
import com.safedriving.entity.Account;
import com.safedriving.entity.enums.AccountRole;
import com.safedriving.entity.enums.AccountStatus;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.AccountRepository;
import com.safedriving.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    @DisplayName("Lấy danh sách tất cả tài khoản - Bỏ qua isDeleted")
    void getAllAccounts_Success() {
        Account acc1 = new Account();
        acc1.setId("acc-1");
        acc1.setUsername("user1");
        acc1.setIsDeleted(false);

        Account acc2 = new Account();
        acc2.setId("acc-2");
        acc2.setUsername("user2");
        acc2.setIsDeleted(true); // Should be filtered out

        when(accountRepository.findAll()).thenReturn(List.of(acc1, acc2));

        List<AccountResponse> result = accountService.getAllAccounts();

        assertEquals(1, result.size());
        assertEquals("user1", result.get(0).getUsername());
    }

    @Test
    @DisplayName("Lấy tài khoản theo ID - Thành công")
    void getAccountById_Success() {
        Account acc = new Account();
        acc.setId("acc-1");
        acc.setUsername("user1");
        acc.setIsDeleted(false);

        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(acc));

        AccountResponse result = accountService.getAccountById("acc-1");

        assertNotNull(result);
        assertEquals("user1", result.getUsername());
    }

    @Test
    @DisplayName("Lấy tài khoản theo ID - Báo lỗi khi đã bị xóa mềm")
    void getAccountById_Deleted_ThrowsException() {
        Account acc = new Account();
        acc.setId("acc-1");
        acc.setIsDeleted(true);

        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(acc));

        assertThrows(ResourceNotFoundException.class, () -> accountService.getAccountById("acc-1"));
    }

    @Test
    @DisplayName("Cập nhật trạng thái tài khoản - Thành công")
    void updateAccountStatus_Success() {
        Account acc = new Account();
        acc.setId("acc-1");
        acc.setStatus(AccountStatus.ACTIVE);
        acc.setIsDeleted(false);

        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(acc));
        when(accountRepository.save(any(Account.class))).thenReturn(acc);

        AccountResponse result = accountService.updateAccountStatus("acc-1", AccountStatus.LOCKED);

        assertEquals(AccountStatus.LOCKED, acc.getStatus());
        assertNotNull(result);
        verify(accountRepository).save(acc);
    }

    @Test
    @DisplayName("Cập nhật quyền tài khoản - Thành công")
    void updateAccountRole_Success() {
        Account acc = new Account();
        acc.setId("acc-1");
        acc.setRole(AccountRole.DRIVER);
        acc.setIsDeleted(false);

        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(acc));
        when(accountRepository.save(any(Account.class))).thenReturn(acc);

        AccountResponse result = accountService.updateAccountRole("acc-1", AccountRole.ADMIN);

        assertEquals(AccountRole.ADMIN, acc.getRole());
        assertNotNull(result);
        verify(accountRepository).save(acc);
    }

    @Test
    @DisplayName("Xóa tài khoản - Thành công (Soft delete)")
    void deleteAccount_Success() {
        Account acc = new Account();
        acc.setId("acc-1");
        acc.setIsDeleted(false);

        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(acc));

        accountService.deleteAccount("acc-1");

        assertTrue(acc.getIsDeleted());
        verify(accountRepository).save(acc);
    }
}
