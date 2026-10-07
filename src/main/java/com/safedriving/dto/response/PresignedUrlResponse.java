package com.safedriving.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Phản hồi Presigned URL để upload ảnh trực tiếp lên MinIO/S3")
public class PresignedUrlResponse {

    @JsonProperty("upload_url")
    @Schema(description = "Presigned URL để Mobile thực hiện HTTP PUT upload ảnh trực tiếp")
    private String uploadUrl;

    @JsonProperty("public_url")
    @Schema(description = "URL công khai của ảnh sau khi upload thành công")
    private String publicUrl;

    @JsonProperty("expires_in")
    @Schema(description = "Thời gian hết hạn của link upload (giây)", example = "3600")
    private Integer expiresIn;
}
