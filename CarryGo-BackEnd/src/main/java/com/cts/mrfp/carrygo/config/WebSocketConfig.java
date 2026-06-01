package com.cts.mrfp.carrygo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

// Configures the WebSocket endpoint that the frontend connects to for real-time updates
// (new ride requests, status changes, chat messages, etc.).
// Clients connect to ws://localhost:8081/ws and subscribe to /topic/... channels.
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Channels clients can subscribe to. /topic = broadcasts, /queue = direct messages.
        registry.enableSimpleBroker("/topic", "/queue");
        // Prefix for messages the client sends TO the server.
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
    }
}