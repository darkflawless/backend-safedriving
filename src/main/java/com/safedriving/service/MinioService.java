package com.safedriving.service;

import com.safedriving.dto.request.PresignedUrlRequest;
import com.safedriving.dto.response.PresignedUrlResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MinioService {

    PresignedUrlResponse generatePresignedUploadUrl(PresignedUrlRequest request);

    String uploadFile(MultipartFile file, String folder);
}
