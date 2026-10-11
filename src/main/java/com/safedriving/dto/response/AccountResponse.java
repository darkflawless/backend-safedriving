package com.safedriving.dto.response;

import com.safedriving.entity.enums.AccountRole;
import com.safedriving.entity.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private String id;
    private String username;
    private AccountStatus status;
    private AccountRole role;
    private Boolean isDeleted;
    private LocalDateTime lastLoginAt;
}
