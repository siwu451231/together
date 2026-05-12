package com.example.backend.collab.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_operations")
public class DocumentOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long documentId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long baseVersion;

    @Column(name = "target_version", nullable = false)
    private Long newVersion;

    @Column(name = "new_version", nullable = false)
    private Long legacyNewVersion;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String contentSnapshot;

    @Lob
    @Column(name = "op_payload", nullable = false, columnDefinition = "LONGTEXT")
    private String opPayload;

    @Column(name = "client_op_id", nullable = false, length = 64)
    private String clientOpId;

    @Column(name = "op_type", nullable = false, length = 20)
    private String opType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (clientOpId == null || clientOpId.isBlank()) {
            clientOpId = UUID.randomUUID().toString().replace("-", "");
        }
        if (newVersion == null && legacyNewVersion != null) {
            newVersion = legacyNewVersion;
        }
        if (legacyNewVersion == null && newVersion != null) {
            legacyNewVersion = newVersion;
        }
        if (opType == null || opType.isBlank()) {
            opType = "replace";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBaseVersion() {
        return baseVersion;
    }

    public void setBaseVersion(Long baseVersion) {
        this.baseVersion = baseVersion;
    }

    public Long getNewVersion() {
        return newVersion != null ? newVersion : legacyNewVersion;
    }

    public void setNewVersion(Long newVersion) {
        this.newVersion = newVersion;
        this.legacyNewVersion = newVersion;
    }

    public Long getLegacyNewVersion() {
        return legacyNewVersion;
    }

    public void setLegacyNewVersion(Long legacyNewVersion) {
        this.legacyNewVersion = legacyNewVersion;
        if (this.newVersion == null) {
            this.newVersion = legacyNewVersion;
        }
    }

    public String getContentSnapshot() {
        return contentSnapshot;
    }

    public void setContentSnapshot(String contentSnapshot) {
        this.contentSnapshot = contentSnapshot;
    }

    public String getOpPayload() {
        return opPayload;
    }

    public void setOpPayload(String opPayload) {
        this.opPayload = opPayload;
    }

    public String getClientOpId() {
        return clientOpId;
    }

    public void setClientOpId(String clientOpId) {
        this.clientOpId = clientOpId;
    }

    public String getOpType() {
        return opType;
    }

    public void setOpType(String opType) {
        this.opType = opType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
