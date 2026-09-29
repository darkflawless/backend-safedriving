package com.safedriving.backend.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quản lý các kết nối WebSocket đang mở (phân biệt giữa Web Admin và App Android của tài xế)
 */
@Component
public class SessionRegistry {

    private static final Logger log = LoggerFactory.getLogger(SessionRegistry.class);

    // Lưu các session từ Web Admin: sessionId -> WebSocketSession
    private final Map<String, WebSocketSession> webSessions = new ConcurrentHashMap<>();

    // Lưu session từ App tài xế: driverId -> WebSocketSession
    private final Map<Long, WebSocketSession> appSessions = new ConcurrentHashMap<>();

    public void registerWebSession(WebSocketSession session) {
        webSessions.put(session.getId(), session);
        log.info("[Registry] Web Admin đã kết nối: {}", session.getId());
    }

    public void removeWebSession(String sessionId) {
        webSessions.remove(sessionId);
        log.info("[Registry] Web Admin đã ngắt kết nối: {}", sessionId);
    }

    public void registerAppSession(Long driverId, WebSocketSession session) {
        appSessions.put(driverId, session);
        log.info("[Registry] App Android đã kết nối: driverId={}", driverId);
    }

    public void removeAppSession(Long driverId) {
        appSessions.remove(driverId);
        log.info("[Registry] App Android đã ngắt kết nối: driverId={}", driverId);
    }

    public WebSocketSession getAppSession(Long driverId) {
        return appSessions.get(driverId);
    }

    public Collection<WebSocketSession> getAllWebSessions() {
        return webSessions.values();
    }
}
