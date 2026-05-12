package com.example.backend.document.dto;

import com.example.backend.document.entity.MemberRole;

import java.time.LocalDateTime;

public class InvitationResponse {
    private Long id;
    private Long documentId;
    private String documentTitle;
    private Long inviterId;
    private String inviterName;
    private MemberRole role;
    private LocalDateTime createdAt;

    public InvitationResponse(Long id, Long documentId, String documentTitle, Long inviterId, String inviterName,
                              MemberRole role, LocalDateTime createdAt) {
        this.id = id;
        this.documentId = documentId;
        this.documentTitle = documentTitle;
        this.inviterId = inviterId;
        this.inviterName = inviterName;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public Long getInviterId() {
        return inviterId;
    }

    public String getInviterName() {
        return inviterName;
    }

    public MemberRole getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
