package com.safedriving.dto.request;

import com.safedriving.entity.enums.HitlStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu phê duyệt hoặc bác bỏ vi phạm an toàn")
public class HitlReviewDecisionRequest {

    @NotNull(message = "Quyết định không được để trống (APPROVED hoặc REJECTED)")
    @Schema(description = "Quyết định xử lý (APPROVED hoặc REJECTED)", example = "APPROVED")
    private HitlStatus decision;

    @Schema(description = "Ghi chú của người kiểm duyệt", example = "Xác nhận tài xế ngủ gật rõ rệt qua ảnh chụp bằng chứng")
    private String notes;
}
