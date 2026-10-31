package com.safedriving.service.impl;

import com.safedriving.dto.response.AccountResponse;
import com.safedriving.entity.Account;
import com.safedriving.entity.enums.AccountRole;
import com.safedriving.entity.enums.AccountStatus;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.AccountRepository;
import com.safedriving.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        log.info("Lấy danh sách tất cả tài khoản (trừ tài khoản đã xóa mềm)");
        return accountRepository.findAll().stream()
                .filter(account -> !Boolean.TRUE.equals(account.getIsDeleted()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(String id) {
        log.info("Xem chi tiết tài khoản với ID: {}", id);
        Account account = findAccountOrThrow(id);
        return toResponse(account);
    }

    @Override
    @Transactional
    public AccountResponse updateAccountStatus(String id, AccountStatus status) {
        log.info("Cập nhật trạng thái tài khoản ID: {} thành {}", id, status);
        Account account = findAccountOrThrow(id);
        account.setStatus(status);
        Account updated = accountRepository.save(account);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public AccountResponse updateAccountRole(String id, AccountRole role) {
        log.info("Cập nhật quyền tài khoản ID: {} thành {}", id, role);
        Account account = findAccountOrThrow(id);
        account.setRole(role);
        Account updated = accountRepository.save(account);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAccount(String id) {
        log.info("Xóa mềm tài khoản với ID: {}", id);
        Account account = findAccountOrThrow(id);
        account.setIsDeleted(true);
        accountRepository.save(account);
    }

    private Account findAccountOrThrow(String id) {
        return accountRepository.findById(id)
                .filter(account -> !Boolean.TRUE.equals(account.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + id));
    }

    private AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .username(account.getUsername())
                .status(account.getStatus())
                .role(account.getRole())
                .isDeleted(account.getIsDeleted())
                .lastLoginAt(account.getLastLoginAt())
                .build();
    }
}
