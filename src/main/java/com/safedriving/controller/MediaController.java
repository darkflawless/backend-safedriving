package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.dto.request.PresignedUrlRequest;
import com.safedriving.dto.response.PresignedUrlResponse;
import com.safedriving.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping({"/media", "/api/v1/media"})
@RequiredArgsConstructor
@Tag(name = "Media Storage (MinIO S3)", description = "APIs Lưu trữ Media và Sinh Presigned URL để Mobile/Web tải ảnh lên S3")
public class MediaController {

    private final MinioService minioService;

    @PostMapping("/presigned")
    @Operation(summary = "Sinh Presigned URL upload ảnh bằng chứng", description = "Tạo một link HTTP PUT tạm thời để Mobile tải ảnh chụp vi phạm trực tiếp lên MinIO/S3.")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUrl(@Valid @RequestBody PresignedUrlRequest request) {
        PresignedUrlResponse response = minioService.generatePresignedUploadUrl(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo Presigned URL thành công", response));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tải trực tiếp ảnh snapshot lên MinIO", description = "Cho phép Mobile/Client gửi multipart form data ảnh vi phạm trực tiếp qua Backend, tránh lỗi Host signature khi gọi S3 từ thiết bị ngoại vi.")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "snapshots") String folder) {
        String publicUrl = minioService.uploadFile(file, folder);
        return ResponseEntity.ok(ApiResponse.success("Tải ảnh snapshot thành công", Map.of("public_url", publicUrl)));
    }
}
