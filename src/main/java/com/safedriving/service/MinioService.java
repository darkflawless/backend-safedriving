package com.safedriving.service;

import com.safedriving.dto.request.PresignedUrlRequest;
import com.safedriving.dto.response.PresignedUrlResponse;

public interface MinioService {

    PresignedUrlResponse generatePresignedUploadUrl(PresignedUrlRequest request);
}
