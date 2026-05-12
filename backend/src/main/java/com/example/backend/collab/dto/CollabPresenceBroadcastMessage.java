package com.example.backend.collab.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CollabPresenceBroadcastMessage {
    private Long documentId;
    private int onlineCount;
    private List<OnlineUser> users;
    private LocalDateTime timestamp;

    public CollabPresenceBroadcastMessage() {
    }

    public CollabPresenceBroadcastMessage(Long documentId, int onlineCount, List<OnlineUser> users, LocalDateTime timestamp) {
        this.documentId = documentId;
        this.onlineCount = onlineCount;
        this.users = users;
        this.timestamp = timestamp;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public int getOnlineCount() {
        return onlineCount;
    }

    public void setOnlineCount(int onlineCount) {
        this.onlineCount = onlineCount;
    }

    public List<OnlineUser> getUsers() {
        return users;
    }

    public void setUsers(List<OnlineUser> users) {
        this.users = users;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public static class OnlineUser {
        private Long userId;
        private String displayName;
        private String color;

        public OnlineUser() {
        }

        public OnlineUser(Long userId, String displayName, String color) {
            this.userId = userId;
            this.displayName = displayName;
            this.color = color;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }
    }
}
