package com.safedriving.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.safedriving.entity.enums.HitlStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin ca vi phạm cần duyệt trong quy trình Human-in-the-Loop")
public class HitlCaseResponse {

    @Schema(description = "ID của ca duyệt", example = "h7777777-aaaa-bbbb-cccc-ddddffff0001")
    private String id;

    @JsonProperty("alert_id")
    @Schema(description = "ID cảnh báo an toàn gốc")
    private String alertId;

    @Schema(description = "Trạng thái duyệt (PENDING, APPROVED, REJECTED)", example = "PENDING")
    private HitlStatus status;

    @JsonProperty("reviewer_id")
    private String reviewerId;

    @JsonProperty("reviewer_username")
    private String reviewerUsername;

    @JsonProperty("review_notes")
    @Schema(description = "Ghi chú của người kiểm duyệt")
    private String reviewNotes;

    @JsonProperty("reviewed_at")
    private LocalDateTime reviewedAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "Chi tiết dữ liệu cảnh báo và ảnh chụp bằng chứng")
    private SafetyAlertDetailResponse alert;
}
