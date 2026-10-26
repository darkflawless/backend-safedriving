package com.safedriving.service;

import java.util.Map;

public interface GpsSimulationService {

    /**
     * Lấy dữ liệu lộ trình 9.4 km (393 tọa độ) kèm danh sách điểm đen tai nạn dọc tuyến.
     */
    Map<String, Object> getRouteData();

    /**
     * Khởi chạy bộ mô phỏng GPS xe di chuyển từ Bến xe Mỹ Đình về Nhà Hát Lớn.
     *
     * @param tripId ID của chuyến đi
     * @param speedMultiplier Hệ số tăng tốc mô phỏng (ví dụ: 1.0, 2.0, 5.0)
     * @param startIndex Tùy chọn điểm bắt đầu (ví dụ: 105 để test ngay Điểm đen 1, 215 cho Điểm đen 2)
     */
    Map<String, Object> startSimulation(String tripId, Double speedMultiplier, Integer startIndex);

    /**
     * Dừng mô phỏng GPS.
     */
    Map<String, Object> stopSimulation();

    /**
     * Lấy trạng thái hiện tại của bộ mô phỏng (đang chạy/dừng, tọa độ hiện tại, index).
     */
    Map<String, Object> getStatus();
}
