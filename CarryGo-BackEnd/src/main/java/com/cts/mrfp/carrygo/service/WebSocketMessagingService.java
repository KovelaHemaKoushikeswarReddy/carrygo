package com.cts.mrfp.carrygo.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

// Thin wrapper around Spring's WebSocket messaging template.
// Other services call these methods to send real-time updates to the frontend.
@Service
public class WebSocketMessagingService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketMessagingService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Tells every online porter that there's a new ride request.
    public void pushToPorters(String eventName, Object payload) {
        messagingTemplate.convertAndSend("/topic/new-orders", (Object) wrap(eventName, payload));
    }

    // Sends a private event to one user (delivery status, broadcast progress, etc.).
    public void push(Integer userId, String eventName, Object payload) {
        messagingTemplate.convertAndSend("/topic/user/" + userId, (Object) wrap(eventName, payload));
    }

    // Pushes a chat message to everyone subscribed to that delivery's chat room.
    public void pushToChat(Integer deliveryId, Object payload) {
        messagingTemplate.convertAndSend("/topic/chat/" + deliveryId, (Object) wrap("chatMessage", payload));
    }

    // Wraps every payload as { event, data } so the client can switch on the event name.
    private Map<String, Object> wrap(String eventName, Object payload) {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("event", eventName);
        msg.put("data", payload);
        return msg;
    }
}
