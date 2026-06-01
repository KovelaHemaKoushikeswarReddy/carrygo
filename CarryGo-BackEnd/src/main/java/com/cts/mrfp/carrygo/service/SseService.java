package com.cts.mrfp.carrygo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

// Manages Server-Sent Event streams — one open stream per user.
// Kept as a fallback for clients that can't use WebSockets.
@Service
public class SseService {

    // userId → their open SSE stream. ConcurrentHashMap because many threads may push at once.
    private final ConcurrentHashMap<Integer, SseEmitter> emitters = new ConcurrentHashMap<>();

    // Opens a long-lived stream for one user.
    public SseEmitter subscribe(Integer userId) {
        // 0 = never time out on the server side; the browser controls disconnect.
        SseEmitter emitter = new SseEmitter(0L);

        emitters.put(userId, emitter);

        // Clean up the map whenever the stream ends for any reason.
        emitter.onCompletion(() -> emitters.remove(userId, emitter));
        emitter.onTimeout(()   -> { emitters.remove(userId, emitter); emitter.complete(); });
        emitter.onError(e      -> emitters.remove(userId, emitter));

        // Send a "hello" event so the browser knows the connection is alive.
        try {
            emitter.send(SseEmitter.event().name("ping").data("connected"));
        } catch (IOException ignored) {
            emitters.remove(userId);
        }

        return emitter;
    }

    // Pushes a named event to one user if they have an open stream.
    public void push(Integer userId, String eventName, Object payload) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) return;
        try {
            emitter.send(SseEmitter.event().name(eventName).data(payload));
        } catch (IOException e) {
            emitters.remove(userId, emitter);
        }
    }
}
