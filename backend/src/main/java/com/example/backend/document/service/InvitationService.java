package com.example.backend.document.service;

import com.example.backend.auth.repository.UserRepository;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.document.dto.DocumentResponse;
import com.example.backend.document.dto.InvitationResponse;
import com.example.backend.document.entity.*;
import com.example.backend.document.repository.DocumentInvitationRepository;
import com.example.backend.document.repository.DocumentMemberRepository;
import com.example.backend.document.repository.DocumentRepository;
import com.example.backend.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvitationService {
    private final DocumentService documentService;
    private final DocumentRepository documentRepository;
    private final DocumentMemberRepository documentMemberRepository;
    private final DocumentInvitationRepository invitationRepository;
    private final UserRepository userRepository;

    public InvitationService(DocumentService documentService,
                             DocumentRepository documentRepository,
                             DocumentMemberRepository documentMemberRepository,
                             DocumentInvitationRepository invitationRepository,
                             UserRepository userRepository) {
        this.documentService = documentService;
        this.documentRepository = documentRepository;
        this.documentMemberRepository = documentMemberRepository;
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void invite(Long userId, Long documentId, String username, MemberRole role) {
        Document doc = documentService.getDocument(documentId);
        if (!doc.getOwnerId().equals(userId)) {
            throw new BusinessException(403, "仅文档拥有者可邀请协作者");
        }

        User invitee = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (invitee.getId().equals(userId)) {
            throw new BusinessException("不能邀请自己");
        }

        if (documentMemberRepository.existsByDocumentIdAndUserId(documentId, invitee.getId())) {
            throw new BusinessException("该用户已是文档成员");
        }

        DocumentInvitation invitation = invitationRepository
                .findByDocumentIdAndInviteeId(documentId, invitee.getId())
                .orElseGet(DocumentInvitation::new);

        invitation.setDocumentId(documentId);
        invitation.setInviterId(userId);
        invitation.setInviteeId(invitee.getId());
        invitation.setRole(role == null ? MemberRole.EDITOR : role);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setRespondedAt(null);
        invitationRepository.save(invitation);
    }

    public List<InvitationResponse> listPending(Long userId) {
        List<DocumentInvitation> invitations = invitationRepository
                .findByInviteeIdAndStatusOrderByCreatedAtDesc(userId, InvitationStatus.PENDING);

        return invitations.stream().map(inv -> {
            String inviterName = userRepository.findById(inv.getInviterId())
                    .map(User::getDisplayName)
                    .orElse("未知用户");
            String docTitle = documentRepository.findById(inv.getDocumentId())
                    .map(Document::getTitle)
                    .orElse("已删除文档");
            return new InvitationResponse(
                    inv.getId(),
                    inv.getDocumentId(),
                    docTitle,
                    inv.getInviterId(),
                    inviterName,
                    inv.getRole(),
                    inv.getCreatedAt()
            );
        }).toList();
    }

    @Transactional
    public DocumentResponse accept(Long userId, Long invitationId) {
        DocumentInvitation invitation = invitationRepository.findByIdAndInviteeId(invitationId, userId)
                .orElseThrow(() -> new BusinessException(404, "邀请不存在"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BusinessException("该邀请已处理");
        }

        Long documentId = invitation.getDocumentId();
        DocumentMember member = documentMemberRepository.findByDocumentIdAndUserId(documentId, userId)
                .orElseGet(DocumentMember::new);
        member.setDocumentId(documentId);
        member.setUserId(userId);
        member.setRole(invitation.getRole() == null ? MemberRole.EDITOR : invitation.getRole());
        documentMemberRepository.save(member);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setRespondedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        return documentService.getById(userId, documentId);
    }

    @Transactional
    public void reject(Long userId, Long invitationId) {
        DocumentInvitation invitation = invitationRepository.findByIdAndInviteeId(invitationId, userId)
                .orElseThrow(() -> new BusinessException(404, "邀请不存在"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BusinessException("该邀请已处理");
        }

        invitation.setStatus(InvitationStatus.REJECTED);
        invitation.setRespondedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
    }
}
