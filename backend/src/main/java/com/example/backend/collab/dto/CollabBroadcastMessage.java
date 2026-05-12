package com.example.backend.collab.dto;

import java.time.LocalDateTime;

public class CollabBroadcastMessage {
    private Long documentId;
    private Long version;
    private String content;
    private Long senderId;
    private String senderName;
    private LocalDateTime timestamp;

    public CollabBroadcastMessage(Long documentId, Long version, String content, Long senderId, String senderName,
                                  LocalDateTime timestamp) {
        this.documentId = documentId;
        this.version = version;
        this.content = content;
        this.senderId = senderId;
        this.senderName = senderName;
        this.timestamp = timestamp;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
