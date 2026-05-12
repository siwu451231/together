package com.example.backend.document.dto;

import com.example.backend.document.entity.MemberRole;

import java.time.LocalDateTime;

public class DocumentResponse {
    private Long id;
    private String title;
    private String content;
    private Long ownerId;
    private Long version;
    private MemberRole role;
    private LocalDateTime updatedAt;

    public DocumentResponse(Long id, String title, String content, Long ownerId, Long version, MemberRole role,
                            LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.ownerId = ownerId;
        this.version = version;
        this.role = role;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public MemberRole getRole() {
        return role;
    }

    public void setRole(MemberRole role) {
        this.role = role;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
