package com.safedriving.controller;

import com.safedriving.service.SseBroadcasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping({"/realtime", "/api/v1/realtime"})
@RequiredArgsConstructor
@Tag(name = "Realtime Stream (SSE)", description = "APIs Truyền phát dữ liệu sự kiện cảnh báo và GPS thời gian thực qua Server-Sent Events")
public class RealtimeStreamController {

    private final SseBroadcasterService sseBroadcasterService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Đăng ký luồng sự kiện thời gian thực (SSE Stream)",
            description = "Mở kết nối Server-Sent Events liên tục. Server sẽ tự động đẩy sự kiện cảnh báo ('alert'), GPS xe ('gps') và ping định kỳ mỗi 15 giây.")
    public SseEmitter streamRealtimeEvents() {
        return sseBroadcasterService.createEmitter();
    }
}
