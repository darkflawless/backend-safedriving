package com.safedriving.backend.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Đăng ký endpoint /ws/realtime để client kết nối.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final RealTimeHandler realTimeHandler;

    public WebSocketConfig(RealTimeHandler realTimeHandler) {
        this.realTimeHandler = realTimeHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(realTimeHandler, "/ws/realtime")
                .setAllowedOrigins("*");
    }
}
