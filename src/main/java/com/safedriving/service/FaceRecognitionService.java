package com.safedriving.service;

import org.springframework.web.multipart.MultipartFile;

public interface FaceRecognitionService {

    /**
     * Gửi ảnh khuôn mặt sang FastAPI để trích xuất embedding và lưu vào model database (.pkl)
     *
     * @param driverId ID định danh của tài xế (UUID)
     * @param image    File ảnh chụp khuôn mặt (JPEG/PNG)
     * @param overwrite Ghi đè nếu driverId đã tồn tại
     */
    void enrollDriver(String driverId, MultipartFile image, boolean overwrite);

    /**
     * Xóa dữ liệu khuôn mặt của tài xế khỏi FastAPI và file .pkl
     *
     * @param driverId ID định danh của tài xế (UUID)
     */
    void deleteDriver(String driverId);
}
