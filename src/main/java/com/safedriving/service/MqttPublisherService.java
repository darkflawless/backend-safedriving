package com.safedriving.service;

/**
 * Dịch vụ phát sóng thông điệp MQTT từ Backend tới Broker (EMQX).
 * Phục vụ gửi cảnh báo điểm đen tai nạn xuống ứng dụng Mobile (AppDetect).
 */
public interface MqttPublisherService {

    /**
     * Xuất bản thông điệp tới một topic cụ thể.
     *
     * @param topic Topic MQTT (ví dụ: driverguard/warnings/{vehicleId})
     * @param payload Nội dung thông điệp (chuỗi JSON)
     */
    void publish(String topic, String payload);
}
