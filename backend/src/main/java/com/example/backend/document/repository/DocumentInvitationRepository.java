package com.example.backend.document.repository;

import com.example.backend.document.entity.DocumentInvitation;
import com.example.backend.document.entity.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentInvitationRepository extends JpaRepository<DocumentInvitation, Long> {
    List<DocumentInvitation> findByInviteeIdAndStatusOrderByCreatedAtDesc(Long inviteeId, InvitationStatus status);

    Optional<DocumentInvitation> findByIdAndInviteeId(Long id, Long inviteeId);

    Optional<DocumentInvitation> findByDocumentIdAndInviteeId(Long documentId, Long inviteeId);

    void deleteByDocumentId(Long documentId);
}
