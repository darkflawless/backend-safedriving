package com.safedriving.service.impl;

import com.safedriving.service.SseBroadcasterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class SseBroadcasterServiceImpl implements SseBroadcasterService {

    private static final Long DEFAULT_TIMEOUT = 30 * 60 * 1000L; // 30 phút
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @Override
    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            log.debug("SSE connection completed. Còn lại {} kết nối", emitters.size());
        });

        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            log.debug("SSE connection timed out. Còn lại {} kết nối", emitters.size());
        });

        emitter.onError(e -> {
            emitters.remove(emitter);
            log.debug("SSE connection error: {}. Còn lại {} kết nối", e.getMessage(), emitters.size());
        });

        emitters.add(emitter);
        log.info("Client mới kết nối SSE stream. Tổng kết nối: {}", emitters.size());

        // Gửi thông điệp chào mừng
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of(
                            "message", "Kết nối SSE Realtime Stream thành công",
                            "timestamp", System.currentTimeMillis()
                    )));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    @Override
    public void broadcast(String eventName, Object data) {
        if (emitters.isEmpty()) {
            return;
        }

        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        }

        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
            log.debug("Đã xóa {} SSE kết nối không còn khả dụng", deadEmitters.size());
        }
    }

    @Scheduled(fixedRate = 15000)
    public void sendHeartbeat() {
        if (emitters.isEmpty()) {
            return;
        }

        broadcast("ping", Map.of(
                "type", "heartbeat",
                "timestamp", System.currentTimeMillis(),
                "active_clients", emitters.size()
        ));
    }

    @Override
    public int getActiveEmitterCount() {
        return emitters.size();
    }
}
