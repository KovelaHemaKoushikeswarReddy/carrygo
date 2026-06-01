package com.cts.mrfp.carrygo.controller;

import com.cts.mrfp.carrygo.service.SseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// Server-Sent Events endpoint — kept around as a fallback for clients that can't use WebSockets.
// The frontend mainly uses the WebSocket connection (see WebSocketConfig) for live updates.
@RestController
@RequestMapping("/api/sse")
public class SseController {

    @Autowired private SseService sseService;

    // GET /api/sse/subscribe/{userId} — opens a long-lived HTTP stream
    // that pushes events to the browser whenever something happens for this user.
    @GetMapping(value = "/subscribe/{userId}", produces = "text/event-stream")
    public SseEmitter subscribe(
            @PathVariable Integer userId,
            @RequestHeader(name = "Origin", required = false, defaultValue = "") String origin) {
        return sseService.subscribe(userId);
    }
}
