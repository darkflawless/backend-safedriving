package com.safedriving.dto.response;

import com.safedriving.entity.enums.AccountRole;
import com.safedriving.entity.enums.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin tóm tắt tài khoản liên kết với nhân sự")
public class StaffAccountInfo {

    @Schema(description = "ID tài khoản")
    private String id;

    @Schema(description = "Tên đăng nhập")
    private String username;

    @Schema(description = "Vai trò người dùng")
    private AccountRole role;

    @Schema(description = "Trạng thái tài khoản")
    private AccountStatus status;
}
