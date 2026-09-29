package com.safedriving.backend.websocket;

import org.springframework.stereotype.Service;

/**
 * Lớp đóng gói payload JSON chuẩn và gọi RealTimeHandler phát tới Web.
 */
@Service
public class RealTimeBroadcaster {

    private final RealTimeHandler realTimeHandler;

    public RealTimeBroadcaster(RealTimeHandler realTimeHandler) {
        this.realTimeHandler = realTimeHandler;
    }

    /** Gửi frame ảnh camera dạng Base64 đến Web Admin */
    public void sendCameraFrame(Long driverId, String base64Image) {
        String json = String.format(
                "{\"type\":\"CAMERA_FRAME\",\"driverId\":%d,\"data\":\"%s\"}",
                driverId, base64Image);
        realTimeHandler.broadcastToWeb(json);
    }

    /** Gửi vị trí GPS */
    public void sendGpsLocation(Long vehicleId, double lat, double lng) {
        String json = String.format(
                "{\"type\":\"GPS_LOCATION\",\"vehicleId\":%d,\"lat\":%.6f,\"lng\":%.6f}",
                vehicleId, lat, lng);
        realTimeHandler.broadcastToWeb(json);
    }

    /** Gửi cảnh báo buồn ngủ, cồn... */
    public void sendAlert(Long driverId, String alertMsg) {
        String json = String.format(
                "{\"type\":\"ALERT\",\"driverId\":%d,\"message\":\"%s\"}",
                driverId, alertMsg);
        realTimeHandler.broadcastToWeb(json);
    }
}
