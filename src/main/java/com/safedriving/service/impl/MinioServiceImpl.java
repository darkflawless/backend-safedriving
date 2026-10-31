package com.safedriving.service.impl;

import com.safedriving.dto.request.PresignedUrlRequest;
import com.safedriving.dto.response.PresignedUrlResponse;
import com.safedriving.exception.BadRequestException;
import com.safedriving.service.MinioService;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MinioServiceImpl implements MinioService {

    @Value("${minio.endpoint:http://localhost:9000}")
    private String endpoint;

    @Value("${minio.access-key:minioadmin}")
    private String accessKey;

    @Value("${minio.secret-key:minioadminpassword}")
    private String secretKey;

    @Value("${minio.bucket-name:driverguard-media}")
    private String bucketName;

    @Value("${minio.expiry-seconds:3600}")
    private int expirySeconds;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        try {
            this.minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("✅ Đã tự động tạo MinIO Bucket: {}", bucketName);
            } else {
                log.info("✅ MinIO Bucket '{}' đã tồn tại và sẵn sàng", bucketName);
            }

            // Đặt policy anonymous download (Public Read) để trình duyệt web hiển thị trực tiếp ảnh
            try {
                String config = """
                        {
                          "Version": "2012-10-17",
                          "Statement": [
                            {
                              "Effect": "Allow",
                              "Principal": {"AWS": ["*"]},
                              "Action": ["s3:GetObject"],
                              "Resource": ["arn:aws:s3:::%s/*"]
                            }
                          ]
                        }
                        """.formatted(bucketName);
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(config)
                                .build()
                );
                log.info("✅ Đã kích hoạt Public Read cho MinIO Bucket: {}", bucketName);
            } catch (Exception pe) {
                log.warn("⚠️ Không thể set public policy cho bucket {}: {}", bucketName, pe.getMessage());
            }
        } catch (Exception e) {
            log.warn("⚠️ Không thể kết nối hoặc khởi tạo MinIO Bucket '{}': {}. Sẽ thử lại khi có request.", bucketName, e.getMessage());
        }
    }

    @Override
    public PresignedUrlResponse generatePresignedUploadUrl(PresignedUrlRequest request) {
        try {
            if (minioClient == null) {
                init();
            }

            String folder = (request.getFolder() != null && !request.getFolder().isBlank())
                    ? request.getFolder().replaceAll("^/+|/+$", "")
                    : "snapshots";

            String originalName = request.getFilename();
            String extension = "";
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalName.substring(dotIndex);
            } else {
                extension = ".jpg";
            }

            String timestampStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String objectName = String.format("%s/alert_snapshot_%s_%s%s",
                    folder, timestampStr, UUID.randomUUID().toString().substring(0, 8), extension);

            String uploadUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expirySeconds, TimeUnit.SECONDS)
                            .build()
            );

            String cleanEndpoint = endpoint.replaceAll("/+$", "");
            String publicUrl = String.format("%s/%s/%s", cleanEndpoint, bucketName, objectName);

            return PresignedUrlResponse.builder()
                    .uploadUrl(uploadUrl)
                    .publicUrl(publicUrl)
                    .expiresIn(expirySeconds)
                    .build();

        } catch (Exception e) {
            log.error("Lỗi khi sinh Presigned URL từ MinIO: {}", e.getMessage(), e);
            throw new BadRequestException("Không thể tạo link upload hình ảnh: " + e.getMessage());
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            if (minioClient == null) {
                init();
            }

            if (file == null || file.isEmpty()) {
                throw new BadRequestException("File ảnh tải lên không được rỗng");
            }

            String cleanFolder = (folder != null && !folder.isBlank())
                    ? folder.replaceAll("^/+|/+$", "")
                    : "snapshots";

            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "snapshot.jpg";
            String extension = ".jpg";
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalName.substring(dotIndex);
            }

            String timestampStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String objectName = String.format("%s/alert_snapshot_%s_%s%s",
                    cleanFolder, timestampStr, UUID.randomUUID().toString().substring(0, 8), extension);

            String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );

            String cleanEndpoint = endpoint.replaceAll("/+$", "");
            String publicUrl = String.format("%s/%s/%s", cleanEndpoint, bucketName, objectName);
            log.info("📸 Đã lưu thành công snapshot lên MinIO S3: {}", publicUrl);
            return publicUrl;

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi khi upload file lên MinIO: {}", e.getMessage(), e);
            throw new BadRequestException("Không thể lưu hình ảnh snapshot: " + e.getMessage());
        }
    }
}
