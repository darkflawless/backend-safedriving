package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.dto.request.PresignedUrlRequest;
import com.safedriving.dto.response.PresignedUrlResponse;
import com.safedriving.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/media", "/api/v1/media"})
@RequiredArgsConstructor
@Tag(name = "Media Storage (MinIO S3)", description = "APIs Sinh Presigned URL để Mobile/Web upload file trực tiếp lên S3")
public class MediaController {

    private final MinioService minioService;

    @PostMapping("/presigned")
    @Operation(summary = "Sinh Presigned URL upload ảnh bằng chứng", description = "Tạo một link HTTP PUT tạm thời để Mobile tải ảnh chụp vi phạm trực tiếp lên MinIO/S3.")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUrl(@Valid @RequestBody PresignedUrlRequest request) {
        PresignedUrlResponse response = minioService.generatePresignedUploadUrl(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo Presigned URL thành công", response));
    }
}
