package com.safedriving.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safedriving.exception.BadRequestException;
import com.safedriving.service.FaceRecognitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class FaceRecognitionServiceImpl implements FaceRecognitionService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final boolean enabled;

    public FaceRecognitionServiceImpl(
            @Value("${face-recognition.enabled:false}") boolean enabled,
            @Value("${face-recognition.base-url:http://localhost:8000}") String baseUrl,
            ObjectMapper objectMapper) {
        this.enabled = enabled;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public void enrollDriver(String driverId, MultipartFile image, boolean overwrite) {
        if (!enabled) {
            log.info("Tính năng nhận diện khuôn mặt FastAPI đang tắt (face-recognition.enabled=false). Bỏ qua enroll cho tài xế [{}]", driverId);
            return;
        }

        if (image == null || image.isEmpty()) {
            throw new BadRequestException("File ảnh không được để trống khi đăng ký khuôn mặt");
        }

        try {
            log.info("Gửi yêu cầu enroll khuôn mặt tài xế [{}] sang FastAPI...", driverId);

            // Chuyển MultipartFile thành ByteArrayResource có kèm tên file để FormHttpMessageConverter gửi đúng multipart
            ByteArrayResource imageResource = new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename() != null ? image.getOriginalFilename() : "avatar.jpg";
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("driver_id", driverId);
            body.add("overwrite", String.valueOf(overwrite));
            body.add("image", imageResource);

            restClient.post()
                    .uri("/api/enroll")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                        String errorBody = new String(resp.getBody().readAllBytes());
                        log.error("FastAPI trả về lỗi 4xx khi enroll [{}]: {}", driverId, errorBody);
                        String detailMessage = extractErrorMessage(errorBody);
                        throw new BadRequestException("Lỗi đăng ký khuôn mặt (AI): " + detailMessage);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                        String errorBody = new String(resp.getBody().readAllBytes());
                        log.error("FastAPI trả về lỗi 5xx khi enroll [{}]: {}", driverId, errorBody);
                        throw new BadRequestException("Hệ thống nhận diện khuôn mặt FastAPI gặp sự cố máy chủ");
                    })
                    .toBodilessEntity();

            log.info("Đã đăng ký thành công khuôn mặt tài xế [{}] vào hệ thống AI", driverId);

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Không thể kết nối hoặc gửi ảnh sang FastAPI Service: {}", e.getMessage(), e);
            throw new BadRequestException("Không thể kết nối đến máy chủ nhận diện khuôn mặt (FastAPI): " + e.getMessage());
        }
    }

    @Override
    public void deleteDriver(String driverId) {
        if (!enabled) {
            log.info("Tính năng nhận diện khuôn mặt FastAPI đang tắt (face-recognition.enabled=false). Bỏ qua xóa khuôn mặt tài xế [{}]", driverId);
            return;
        }

        try {
            log.info("Gửi yêu cầu xóa khuôn mặt tài xế [{}] khỏi FastAPI...", driverId);
            restClient.delete()
                    .uri("/api/drivers/{driverId}", driverId)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (req, resp) -> {
                        log.warn("Tài xế [{}] chưa từng enroll trong FastAPI, bỏ qua xóa", driverId);
                    })
                    .toBodilessEntity();
            log.info("Đã xóa tài xế [{}] khỏi model FastAPI", driverId);
        } catch (Exception e) {
            log.warn("Không thể xóa tài xế [{}] khỏi FastAPI: {}", driverId, e.getMessage());
        }
    }

    private String extractErrorMessage(String errorBody) {
        try {
            JsonNode root = objectMapper.readTree(errorBody);
            if (root.has("detail")) {
                return root.get("detail").asText();
            }
        } catch (Exception ignored) {
        }
        return errorBody;
    }
}
