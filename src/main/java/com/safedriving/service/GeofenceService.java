package com.safedriving.service;

import com.safedriving.entity.AccidentBlackspot;

import java.util.List;

public interface GeofenceService {

    /**
     * Lấy danh sách tất cả các điểm đen tai nạn đang được kích hoạt.
     */
    List<AccidentBlackspot> getActiveBlackspots();

    /**
     * Kiểm tra vị trí hiện tại của phương tiện so với các vùng nguy hiểm (Geofence).
     * Nếu khoảng cách <= bán kính điểm đen, tự động kích hoạt cảnh báo tới Mobile & Web.
     *
     * @param vehicleId ID hoặc biển số của xe
     * @param currentLat Vĩ độ hiện tại
     * @param currentLng Kinh độ hiện tại
     * @param speedKmh Tốc độ hiện tại (km/h)
     */
    void checkBlackspotGeofence(String vehicleId, double currentLat, double currentLng, Double speedKmh);
}
