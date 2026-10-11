package com.safedriving.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tham chiếu ID của entity liên kết")
public class IdReference {

    @Schema(description = "Mã định danh ID", example = "addr-uuid-1")
    private String id;
}
