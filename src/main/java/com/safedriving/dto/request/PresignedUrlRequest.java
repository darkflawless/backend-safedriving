package com.safedriving.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo Presigned URL upload ảnh bằng chứng")
public class PresignedUrlRequest {

    @NotBlank(message = "Tên file không được để trống")
    @Schema(description = "Tên file (ví dụ: alert_snapshot_20261004_023512.jpg)", example = "alert_snapshot_20261004_023512.jpg")
    private String filename;

    @JsonProperty("content_type")
    @Builder.Default
    @Schema(description = "MIME type của ảnh", example = "image/jpeg")
    private String contentType = "image/jpeg";

    @Builder.Default
    @Schema(description = "Thư mục lưu trữ trên bucket (mặc định: snapshots)", example = "snapshots")
    private String folder = "snapshots";
}
