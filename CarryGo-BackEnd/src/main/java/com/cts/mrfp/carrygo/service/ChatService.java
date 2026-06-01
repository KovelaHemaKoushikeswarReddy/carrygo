package com.cts.mrfp.carrygo.service;

import com.cts.mrfp.carrygo.dto.ChatMessageDTO;
import com.cts.mrfp.carrygo.model.ChatMessage;
import com.cts.mrfp.carrygo.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// Saves chat messages to the database and pushes them over WebSocket
// so both sides see the message in real time.
@Service
public class ChatService {

    @Autowired private ChatMessageRepository repo;
    @Autowired private WebSocketMessagingService wsService;

    // Convenience overload — defaults to a plain TEXT message with no specific receiver.
    public ChatMessageDTO send(Integer deliveryId, Integer senderId,
                               String senderName, String senderRole, String message) {
        return send(deliveryId, senderId, null, senderName, senderRole, "TEXT", message);
    }

    // Saves a chat row, then live-pushes it to anyone subscribed to /topic/chat/{deliveryId}.
    public ChatMessageDTO send(Integer deliveryId, Integer senderId, Integer receiverId,
                               String senderName, String senderRole, String messageType, String message) {
        ChatMessage msg = new ChatMessage();
        msg.setDeliveryId(deliveryId);
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setSenderName(senderName);
        msg.setSenderRole(senderRole);
        msg.setMessageType(messageType != null ? messageType : "TEXT");
        msg.setMessage(message);
        msg.setSentAt(LocalDateTime.now());
        msg.setIsRead(false);
        ChatMessageDTO dto = toDTO(repo.save(msg));
        wsService.pushToChat(deliveryId, dto);
        return dto;
    }

    // Returns all past messages for a delivery in the order they were sent.
    public List<ChatMessageDTO> getHistory(Integer deliveryId) {
        return repo.findByDeliveryIdOrderBySentAtAsc(deliveryId)
                   .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // Maps the entity to the DTO that we send to the frontend.
    public ChatMessageDTO toDTO(ChatMessage m) {
        ChatMessageDTO d = new ChatMessageDTO();
        d.setId(m.getId());
        d.setDeliveryId(m.getDeliveryId());
        d.setSenderId(m.getSenderId());
        d.setReceiverId(m.getReceiverId());
        d.setSenderName(m.getSenderName());
        d.setSenderRole(m.getSenderRole());
        d.setMessageType(m.getMessageType());
        d.setMessage(m.getMessage());
        d.setSentAt(m.getSentAt());
        d.setIsRead(m.getIsRead());
        d.setReadAt(m.getReadAt());
        return d;
    }
}
