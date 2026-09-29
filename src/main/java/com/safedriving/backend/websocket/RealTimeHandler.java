package com.safedriving.backend.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Xử lý bắt sự kiện mở/đóng kết nối WebSocket và chuyển tiếp lệnh từ Web xuống App
 */
@Component
public class RealTimeHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(RealTimeHandler.class);

    private final SessionRegistry registry;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RealTimeHandler(SessionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String query = getQuery(session);
        if (isAppClient(query)) {
            Long driverId = extractDriverId(query);
            if (driverId != null) {
                registry.registerAppSession(driverId, session);
            }
        } else {
            registry.registerWebSession(session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String query = getQuery(session);
        if (isAppClient(query)) {
            Long driverId = extractDriverId(query);
            if (driverId != null) {
                registry.removeAppSession(driverId);
            }
        } else {
            registry.removeWebSession(session.getId());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("[WS] Lỗi transport {}: {}", session.getId(), exception.getMessage());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JsonNode json = objectMapper.readTree(message.getPayload());
            String type = json.get("type").asText();

            switch (type) {
                case "REQUEST_STREAM": {
                    // Admin bấm "Xem camera" -> gửi lệnh START_STREAM xuống App
                    long driverId = json.get("driverId").asLong();
                    forwardToApp(driverId, "{\"type\":\"START_STREAM\"}");
                    log.info("[WS] Admin yêu cầu stream từ driverId={}", driverId);
                    break;
                }
                case "STOP_STREAM": {
                    // Admin bấm "Dừng xem" -> gửi lệnh STOP_STREAM xuống App
                    long driverId = json.get("driverId").asLong();
                    forwardToApp(driverId, "{\"type\":\"STOP_STREAM\"}");
                    log.info("[WS] Admin dừng stream từ driverId={}", driverId);
                    break;
                }
                default:
                    log.warn("[WS] Tin nhắn không xác định: {}", type);
            }
        } catch (Exception e) {
            log.error("[WS] Lỗi xử lý tin nhắn: {}", e.getMessage());
        }
    }

    /**
     * Gửi dữ liệu frame ảnh / GPS / Alert đến TẤT CẢ Web Admin đang kết nối
     */
    public void broadcastToWeb(String jsonMessage) {
        for (WebSocketSession session : registry.getAllWebSessions()) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(jsonMessage));
                }
            } catch (Exception e) {
                log.error("[WS] Lỗi gửi đến web session {}: {}", session.getId(), e.getMessage());
            }
        }
    }

    private void forwardToApp(long driverId, String command) {
        WebSocketSession appSession = registry.getAppSession(driverId);
        if (appSession != null && appSession.isOpen()) {
            try {
                appSession.sendMessage(new TextMessage(command));
            } catch (Exception e) {
                log.error("[WS] Lỗi gửi lệnh đến App driverId={}: {}", driverId, e.getMessage());
            }
        } else {
            log.warn("[WS] Không tìm thấy session cho App driverId={}", driverId);
        }
    }

    private String getQuery(WebSocketSession session) {
        return session.getUri() != null ? session.getUri().getQuery() : "";
    }

    private boolean isAppClient(String query) {
        return query != null && query.contains("type=APP");
    }

    private Long extractDriverId(String query) {
        if (query == null) return null;
        for (String param : query.split("&")) {
            if (param.startsWith("driverId=")) {
                try {
                    return Long.parseLong(param.split("=")[1]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }
}
