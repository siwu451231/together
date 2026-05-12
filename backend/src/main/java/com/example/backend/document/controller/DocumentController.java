package com.example.backend.document.controller;

import com.example.backend.common.response.Result;
import com.example.backend.common.util.SecurityUtils;
import com.example.backend.document.dto.CreateDocumentRequest;
import com.example.backend.document.dto.DocumentResponse;
import com.example.backend.document.dto.InviteMemberRequest;
import com.example.backend.document.dto.UpdateDocumentRequest;
import com.example.backend.document.service.DocumentService;
import com.example.backend.document.service.InvitationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    private final DocumentService documentService;
    private final InvitationService invitationService;

    public DocumentController(DocumentService documentService, InvitationService invitationService) {
        this.documentService = documentService;
        this.invitationService = invitationService;
    }

    @PostMapping
    public Result<DocumentResponse> create(@Valid @RequestBody CreateDocumentRequest request) {
        return Result.ok(documentService.create(SecurityUtils.currentUserId(), request));
    }

    @GetMapping
    public Result<List<DocumentResponse>> list() {
        return Result.ok(documentService.listForUser(SecurityUtils.currentUserId()));
    }

    @GetMapping("/{id}")
    public Result<DocumentResponse> detail(@PathVariable Long id) {
        return Result.ok(documentService.getById(SecurityUtils.currentUserId(), id));
    }

    @PutMapping("/{id}")
    public Result<DocumentResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateDocumentRequest request) {
        return Result.ok(documentService.update(SecurityUtils.currentUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        documentService.delete(SecurityUtils.currentUserId(), id);
        return Result.ok("文档已删除", null);
    }

    @PostMapping("/{id}/delete")
    public Result<Void> deleteByPost(@PathVariable Long id) {
        documentService.delete(SecurityUtils.currentUserId(), id);
        return Result.ok("文档已删除", null);
    }

    @PostMapping("/{id}/invite")
    public Result<Void> invite(@PathVariable Long id, @Valid @RequestBody InviteMemberRequest request) {
        invitationService.invite(SecurityUtils.currentUserId(), id, request.getUsername(), request.getRole());
        return Result.ok("邀请已发送，等待对方处理", null);
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> export(@PathVariable Long id,
                                         @RequestParam(defaultValue = "html") String format) {
        DocumentService.ExportFile exportFile = documentService.export(SecurityUtils.currentUserId(), id, format);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + exportFile.fileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, exportFile.contentType())
                .body(exportFile.content());
    }
}
