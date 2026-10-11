package com.safedriving.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseBroadcasterService {

    SseEmitter createEmitter();

    void broadcast(String eventName, Object data);

    int getActiveEmitterCount();
}
