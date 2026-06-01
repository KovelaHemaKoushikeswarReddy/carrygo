package com.cts.mrfp.carrygo.dto;

import java.time.LocalDateTime;

// JSON shape of a chat message as sent to and from the frontend.
public class ChatMessageDTO {
    private Long id;
    private Integer deliveryId;
    private Integer senderId;
    private Integer receiverId;
    private String senderName;
    private String senderRole;
    private String messageType;
    private String message;
    private LocalDateTime sentAt;
    private Boolean isRead;
    private LocalDateTime readAt;

    public ChatMessageDTO() {}

    public Long getId()                     { return id; }
    public void setId(Long v)               { this.id = v; }

    public Integer getDeliveryId()          { return deliveryId; }
    public void setDeliveryId(Integer v)    { this.deliveryId = v; }

    public Integer getSenderId()            { return senderId; }
    public void setSenderId(Integer v)      { this.senderId = v; }

    public String getSenderName()           { return senderName; }
    public void setSenderName(String v)     { this.senderName = v; }

    public String getSenderRole()           { return senderRole; }
    public void setSenderRole(String v)     { this.senderRole = v; }

    public String getMessage()              { return message; }
    public void setMessage(String v)        { this.message = v; }

    public LocalDateTime getSentAt()        { return sentAt; }
    public void setSentAt(LocalDateTime v)  { this.sentAt = v; }

    public Integer getReceiverId()          { return receiverId; }
    public void setReceiverId(Integer v)    { this.receiverId = v; }

    public String getMessageType()          { return messageType; }
    public void setMessageType(String v)    { this.messageType = v; }

    public Boolean getIsRead()              { return isRead; }
    public void setIsRead(Boolean v)        { this.isRead = v; }

    public LocalDateTime getReadAt()        { return readAt; }
    public void setReadAt(LocalDateTime v)  { this.readAt = v; }
}
