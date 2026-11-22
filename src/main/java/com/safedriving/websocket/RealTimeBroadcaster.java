package com.safedriving.websocket;

import org.springframework.stereotype.Service;

/**
 * Lớp đóng gói payload JSON chuẩn và gọi RealTimeHandler phát tới Web.
 *
 * @author phanthuy0811
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

    /** Gửi cảnh báo tài xế vi phạm nồng độ cồn trước chuyến đi */
    public void sendAlcoholViolationAlert(String tripId, String driverId, String driverName, java.math.BigDecimal alcoholLevel) {
        String json = String.format(
                "{\"type\":\"ALCOHOL_VIOLATION_ALERT\",\"tripId\":\"%s\",\"driverId\":\"%s\",\"driverName\":\"%s\",\"alcoholLevel\":%s,\"message\":\"CẢNH BÁO: Tài xế %s vi phạm nồng độ cồn (%s mg/L). Chuyến đi %s đã chuyển sang trạng thái REASSIGN_REQUIRED và gỡ bỏ tài xế.\"}",
                tripId != null ? tripId : "",
                driverId != null ? driverId : "",
                driverName != null ? driverName : "",
                alcoholLevel != null ? alcoholLevel.toPlainString() : "0.0",
                driverName != null ? driverName : "N/A",
                alcoholLevel != null ? alcoholLevel.toPlainString() : "0.0",
                tripId != null ? tripId : "");
        realTimeHandler.broadcastToWeb(json);
    }
}
