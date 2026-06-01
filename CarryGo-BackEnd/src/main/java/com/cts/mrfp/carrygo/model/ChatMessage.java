package com.cts.mrfp.carrygo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// One row per chat message exchanged between a sender and a porter during a delivery.
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer deliveryId;
    private Integer senderId;
    private String senderName;

    // "USER" or "PORTER" — used to decide which side of the chat bubble to render.
    private String senderRole;

    private Integer receiverId;

    // "TEXT" / "IMAGE" / "SYSTEM" — most messages are TEXT.
    private String messageType;

    @Column(length = 1000)
    private String message;

    private LocalDateTime sentAt;
    private Boolean isRead = false;
    private LocalDateTime readAt;

    public ChatMessage() {}

    public Long getId()                    { return id; }
    public void setId(Long id)             { this.id = id; }

    public Integer getDeliveryId()         { return deliveryId; }
    public void setDeliveryId(Integer v)   { this.deliveryId = v; }

    public Integer getSenderId()           { return senderId; }
    public void setSenderId(Integer v)     { this.senderId = v; }

    public String getSenderName()          { return senderName; }
    public void setSenderName(String v)    { this.senderName = v; }

    public String getSenderRole()          { return senderRole; }
    public void setSenderRole(String v)    { this.senderRole = v; }

    public String getMessage()             { return message; }
    public void setMessage(String v)       { this.message = v; }

    public LocalDateTime getSentAt()       { return sentAt; }
    public void setSentAt(LocalDateTime v) { this.sentAt = v; }

    public Integer getReceiverId()           { return receiverId; }
    public void setReceiverId(Integer v)     { this.receiverId = v; }

    public String getMessageType()           { return messageType; }
    public void setMessageType(String v)     { this.messageType = v; }

    public Boolean getIsRead()               { return isRead; }
    public void setIsRead(Boolean v)         { this.isRead = v; }

    public LocalDateTime getReadAt()         { return readAt; }
    public void setReadAt(LocalDateTime v)   { this.readAt = v; }
}
