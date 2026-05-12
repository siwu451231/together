package com.example.backend.history.service;

import com.example.backend.collab.entity.DocumentOperation;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.document.dto.DocumentResponse;
import com.example.backend.document.entity.MemberRole;
import com.example.backend.document.dto.UpdateDocumentRequest;
import com.example.backend.document.service.DocumentService;
import com.example.backend.history.repository.DocumentOperationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HistoryService {
    private final DocumentOperationRepository operationRepository;
    private final DocumentService documentService;

    public HistoryService(DocumentOperationRepository operationRepository, DocumentService documentService) {
        this.operationRepository = operationRepository;
        this.documentService = documentService;
    }

    public List<DocumentOperation> list(Long userId, Long documentId) {
        MemberRole role = documentService.getRole(userId, documentId);
        if (role == null) {
            throw new BusinessException(403, "无权查看历史记录");
        }
        return operationRepository.findTop100ByDocumentIdOrderByIdDesc(documentId);
    }

    @Transactional
    public DocumentResponse rollback(Long userId, Long documentId, Long operationId) {
        MemberRole role = documentService.getRole(userId, documentId);
        if (role == null) {
            throw new BusinessException(403, "无权回退此文档");
        }

        DocumentOperation target = operationRepository.findByIdAndDocumentId(operationId, documentId)
                .orElseThrow(() -> new BusinessException(404, "历史版本不存在"));

        UpdateDocumentRequest request = new UpdateDocumentRequest();
        String rollbackContent = target.getOpPayload();
        if (rollbackContent == null || rollbackContent.isBlank()) {
            rollbackContent = target.getContentSnapshot();
        }
        request.setContent(rollbackContent == null ? "" : rollbackContent);
        return documentService.update(userId, documentId, request);
    }
}