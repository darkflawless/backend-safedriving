package com.safedriving.backend.udp;

import com.safedriving.backend.websocket.RealTimeBroadcaster;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Đọc cấu hình từ file application.yml và khởi tạo Bean UdpCameraReceiver.
 */
@Configuration
public class UdpServerConfig {

    @Value("${udp.camera.port:9090}")
    private int udpPort;

    @Value("${udp.camera.buffer-size:65507}")
    private int bufferSize;

    @Bean
    public UdpCameraReceiver udpCameraReceiver(RealTimeBroadcaster broadcaster) {
        return new UdpCameraReceiver(udpPort, bufferSize, broadcaster);
    }
}
