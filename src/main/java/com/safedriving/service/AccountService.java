package com.safedriving.service;

import com.safedriving.dto.response.AccountResponse;
import com.safedriving.entity.enums.AccountRole;
import com.safedriving.entity.enums.AccountStatus;

import java.util.List;

public interface AccountService {
    List<AccountResponse> getAllAccounts();
    AccountResponse getAccountById(String id);
    AccountResponse updateAccountStatus(String id, AccountStatus status);
    AccountResponse updateAccountRole(String id, AccountRole role);
    void deleteAccount(String id);
}
