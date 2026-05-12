package com.example.backend.collab.dto;

import java.time.LocalDateTime;

public class CollabChatBroadcastMessage {
    private Long documentId;
    private Long senderId;
    private String senderName;
    private String content;
    private String color;
    private LocalDateTime timestamp;

    public CollabChatBroadcastMessage(Long documentId, Long senderId, String senderName, String content, String color, LocalDateTime timestamp) {
        this.documentId = documentId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.color = color;
        this.timestamp = timestamp;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
