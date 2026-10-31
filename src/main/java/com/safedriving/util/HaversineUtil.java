package com.safedriving.util;

/**
 * Tiện ích tính toán khoảng cách tọa độ địa lý theo công thức Haversine.
 * Phục vụ Geofencing phát hiện xe tiến vào vùng bán kính nguy hiểm của Điểm đen tai nạn.
 */
public final class HaversineUtil {

    private static final double EARTH_RADIUS_METERS = 6371000.0;

    private HaversineUtil() {}

    /**
     * Tính khoảng cách trắc địa giữa 2 điểm GPS (đơn vị: mét).
     *
     * @param lat1 Vĩ độ điểm 1
     * @param lon1 Kinh độ điểm 1
     * @param lat2 Vĩ độ điểm 2
     * @param lon2 Kinh độ điểm 2
     * @return Khoảng cách tính bằng mét
     */
    public static double calculateDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }
}
