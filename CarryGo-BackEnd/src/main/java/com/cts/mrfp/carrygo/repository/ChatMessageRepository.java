package com.cts.mrfp.carrygo.repository;

import com.cts.mrfp.carrygo.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// JPA queries for chat messages.
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // All messages for one delivery, oldest first — used to render chat history.
    List<ChatMessage> findByDeliveryIdOrderBySentAtAsc(Integer deliveryId);
}
