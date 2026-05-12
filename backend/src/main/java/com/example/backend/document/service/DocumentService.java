package com.example.backend.document.service;

import com.example.backend.auth.repository.UserRepository;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.document.dto.CreateDocumentRequest;
import com.example.backend.document.dto.DocumentResponse;
import com.example.backend.document.dto.InviteMemberRequest;
import com.example.backend.document.dto.UpdateDocumentRequest;
import com.example.backend.document.entity.Document;
import com.example.backend.document.entity.DocumentMember;
import com.example.backend.document.entity.MemberRole;
import com.example.backend.document.repository.DocumentInvitationRepository;
import com.example.backend.document.repository.DocumentMemberRepository;
import com.example.backend.document.repository.DocumentRepository;
import com.example.backend.collab.entity.DocumentOperation;
import com.example.backend.history.repository.DocumentOperationRepository;
import com.example.backend.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class DocumentService {
    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);
    private static final String SNAPSHOT_MARKER = "S";

    private final DocumentRepository documentRepository;
    private final DocumentMemberRepository documentMemberRepository;
    private final DocumentInvitationRepository documentInvitationRepository;
    private final UserRepository userRepository;
    private final DocumentOperationRepository operationRepository;
    private final JdbcTemplate jdbcTemplate;

    public DocumentService(DocumentRepository documentRepository,
                           DocumentMemberRepository documentMemberRepository,
                           DocumentInvitationRepository documentInvitationRepository,
                           UserRepository userRepository,
                           DocumentOperationRepository operationRepository,
                           JdbcTemplate jdbcTemplate) {
        this.documentRepository = documentRepository;
        this.documentMemberRepository = documentMemberRepository;
        this.documentInvitationRepository = documentInvitationRepository;
        this.userRepository = userRepository;
        this.operationRepository = operationRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public DocumentResponse create(Long userId, CreateDocumentRequest request) {
        Document doc = new Document();
        doc.setTitle(request.getTitle());
        doc.setContent(request.getContent() == null ? "" : request.getContent());
        doc.setOwnerId(userId);
        doc.setVersion(0L);
        doc = documentRepository.save(doc);

        DocumentMember owner = new DocumentMember();
        owner.setDocumentId(doc.getId());
        owner.setUserId(userId);
        owner.setRole(MemberRole.OWNER);
        documentMemberRepository.save(owner);

        return toResponse(doc, MemberRole.OWNER);
    }

    public List<DocumentResponse> listForUser(Long userId) {
        List<Document> owned = documentRepository.findByOwnerIdOrderByUpdatedAtDesc(userId);
        List<DocumentMember> joined = documentMemberRepository.findByUserId(userId);
        Map<Long, MemberRole> roleMap = new LinkedHashMap<>();
        List<Long> joinedIds = new ArrayList<>();

        for (Document doc : owned) {
            roleMap.put(doc.getId(), MemberRole.OWNER);
        }
        for (DocumentMember member : joined) {
            if (!roleMap.containsKey(member.getDocumentId())) {
                roleMap.put(member.getDocumentId(), member.getRole());
                joinedIds.add(member.getDocumentId());
            }
        }

        List<Document> docs = new ArrayList<>(owned);
        if (!joinedIds.isEmpty()) {
            docs.addAll(documentRepository.findAllById(joinedIds));
        }

        return docs.stream()
            .sorted(Comparator.comparing(Document::getUpdatedAt, Comparator.nullsFirst(Comparator.naturalOrder())).reversed())
                .map(doc -> toResponse(doc, roleMap.get(doc.getId())))
                .toList();
    }

    public DocumentResponse getById(Long userId, Long documentId) {
        Document doc = getDocument(documentId);
        MemberRole role = getRole(userId, documentId);
        if (role == null) {
            throw new BusinessException(403, "无权访问此文档");
        }
        return toResponse(doc, role);
    }

    @Transactional
    public DocumentResponse update(Long userId, Long documentId, UpdateDocumentRequest request) {
        Document doc = getDocument(documentId);
        MemberRole role = getRole(userId, documentId);
        if (role == null) {
            throw new BusinessException(403, "无编辑权限");
        }

        boolean manualSave = Boolean.TRUE.equals(request.getManualSave());
        Long baseVersion = doc.getVersion();
        boolean shouldCreateRevision = false;

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            doc.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            doc.setContent(request.getContent());
            if (manualSave) {
                shouldCreateRevision = shouldCreateManualRevision(doc.getId(), doc.getVersion(), request.getContent());
            }
            if (shouldCreateRevision) {
                doc.setVersion(doc.getVersion() + 1);
            }
        }

        doc = documentRepository.save(doc);

        if (manualSave && shouldCreateRevision) {
            DocumentOperation operation = new DocumentOperation();
            operation.setDocumentId(doc.getId());
            operation.setUserId(userId);
            operation.setBaseVersion(baseVersion);
            operation.setNewVersion(doc.getVersion());
            operation.setContentSnapshot(SNAPSHOT_MARKER);
            operation.setOpPayload(doc.getContent());
            try {
                operationRepository.save(operation);
            } catch (DataIntegrityViolationException ex) {
                // Keep core save flow available even if legacy history column width is inconsistent.
                log.error("Failed to persist document operation history for docId={}, version={}", doc.getId(), doc.getVersion(), ex);
            }
        }

        return toResponse(doc, role);
    }

    private boolean shouldCreateManualRevision(Long documentId, Long currentVersion, String currentContent) {
        String normalizedCurrent = currentContent == null ? "" : currentContent;
        var latestOpt = operationRepository.findTopByDocumentIdOrderByIdDesc(documentId);
        if (latestOpt.isEmpty()) {
            return !normalizedCurrent.isBlank();
        }

        DocumentOperation latest = latestOpt.get();
        Long latestVersion = latest.getNewVersion();
        String latestPayload = latest.getOpPayload() == null ? "" : latest.getOpPayload();

        if (latestVersion != null && latestVersion.equals(currentVersion)) {
            return !latestPayload.equals(normalizedCurrent);
        }

        return true;
    }

    @Transactional
    public void invite(Long userId, Long documentId, InviteMemberRequest request) {
        Document doc = getDocument(documentId);
        if (!doc.getOwnerId().equals(userId)) {
            throw new BusinessException(403, "仅文档拥有者可邀请协作者");
        }

        User invitee = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        DocumentMember member = documentMemberRepository.findByDocumentIdAndUserId(documentId, invitee.getId())
                .orElseGet(DocumentMember::new);
        member.setDocumentId(documentId);
        member.setUserId(invitee.getId());
        member.setRole(request.getRole() == null ? MemberRole.EDITOR : request.getRole());
        documentMemberRepository.save(member);
    }

    @Transactional
    public void delete(Long userId, Long documentId) {
        Document doc = getDocument(documentId);
        if (!doc.getOwnerId().equals(userId)) {
            throw new BusinessException(403, "仅文档拥有者可删除文档");
        }

        // Optional tables from custom schema; ignore when table does not exist.
        tryDeleteOptionalTable("document_presence", documentId);
        tryDeleteOptionalTable("document_revisions", documentId);
        tryDeleteOptionalTable("document_snapshots", documentId);

        documentInvitationRepository.deleteByDocumentId(documentId);
        operationRepository.deleteByDocumentId(documentId);
        documentMemberRepository.deleteByDocumentId(documentId);
        documentRepository.delete(doc);
    }

    private void tryDeleteOptionalTable(String tableName, Long documentId) {
        try {
            jdbcTemplate.update("DELETE FROM " + tableName + " WHERE document_id = ?", documentId);
        } catch (DataAccessException ex) {
            log.debug("Skip cleanup for optional table {}: {}", tableName, ex.getMessage());
        }
    }

    public boolean hasAccess(Long userId, Long documentId) {
        return getRole(userId, documentId) != null;
    }

    public MemberRole getRole(Long userId, Long documentId) {
        return documentMemberRepository.findByDocumentIdAndUserId(documentId, userId)
                .map(DocumentMember::getRole)
                .orElse(null);
    }

    public Document getDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(404, "文档不存在"));
    }

    public DocumentResponse toResponse(Document doc, MemberRole role) {
        return new DocumentResponse(
                doc.getId(),
                doc.getTitle(),
                doc.getContent(),
                doc.getOwnerId(),
                doc.getVersion(),
                role,
                doc.getUpdatedAt()
        );
    }

    public ExportFile export(Long userId, Long documentId, String format) {
        Document doc = getDocument(documentId);
        MemberRole role = getRole(userId, documentId);
        if (role == null) {
            throw new BusinessException(403, "无权导出此文档");
        }

        String normalizedFormat = (format == null || format.isBlank())
                ? "html"
                : format.toLowerCase(Locale.ROOT);
        String safeTitle = sanitizeFileName(doc.getTitle());
        String content = doc.getContent() == null ? "" : doc.getContent();

        String html = """
                <!doctype html>
                <html lang=\"zh-CN\">
                <head>
                  <meta charset=\"UTF-8\" />
                  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />
                  <title>%s</title>
                  <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; max-width: 900px; margin: 40px auto; padding: 0 16px; color: #111; }
                    h1 { font-size: 24px; margin-bottom: 20px; }
                    .content { line-height: 1.7; }
                  </style>
                </head>
                <body>
                  <h1>%s</h1>
                  <div class=\"content\">%s</div>
                </body>
                </html>
                """.formatted(escapeHtml(doc.getTitle()), escapeHtml(doc.getTitle()), content);

            return switch (normalizedFormat) {
                case "txt" -> {
                String plain = stripHtml(content);
                yield new ExportFile(
                    safeTitle + ".txt",
                    "text/plain; charset=UTF-8",
                    plain.getBytes(StandardCharsets.UTF_8)
                );
                }
                case "md", "markdown" -> {
                String markdown = htmlToMarkdown(content);
                yield new ExportFile(
                    safeTitle + ".md",
                    "text/markdown; charset=UTF-8",
                    markdown.getBytes(StandardCharsets.UTF_8)
                );
                }
                case "doc" -> new ExportFile(
                    safeTitle + ".doc",
                    "application/msword",
                    html.getBytes(StandardCharsets.UTF_8)
                );
                default -> new ExportFile(
                    safeTitle + ".html",
                    "text/html; charset=UTF-8",
                    html.getBytes(StandardCharsets.UTF_8)
                );
            };
    }

    private String sanitizeFileName(String raw) {
        String fallback = "文档导出";
        String value = (raw == null || raw.isBlank()) ? fallback : raw;
        return value.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }

    private String stripHtml(String html) {
        return html
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n")
                .replaceAll("<[^>]+>", "")
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&amp;", "&");
    }

            private String htmlToMarkdown(String html) {
            String normalized = html == null ? "" : html;
            normalized = normalized
                .replaceAll("(?is)<h1[^>]*>(.*?)</h1>", "# $1\n\n")
                .replaceAll("(?is)<h2[^>]*>(.*?)</h2>", "## $1\n\n")
                .replaceAll("(?is)<h3[^>]*>(.*?)</h3>", "### $1\n\n")
                .replaceAll("(?is)<strong[^>]*>(.*?)</strong>", "**$1**")
                .replaceAll("(?is)<b[^>]*>(.*?)</b>", "**$1**")
                .replaceAll("(?is)<em[^>]*>(.*?)</em>", "*$1*")
                .replaceAll("(?is)<i[^>]*>(.*?)</i>", "*$1*")
                .replaceAll("(?is)<u[^>]*>(.*?)</u>", "$1")
                .replaceAll("(?is)<li[^>]*>(.*?)</li>", "- $1\n")
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n\n")
                .replaceAll("(?i)<p[^>]*>", "")
                .replaceAll("(?i)</div>", "\n")
                .replaceAll("(?i)<div[^>]*>", "")
                .replaceAll("<[^>]+>", "");

            return normalized
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replaceAll("\n{3,}", "\n\n")
                .trim() + "\n";
            }

    private String escapeHtml(String raw) {
        if (raw == null) return "";
        return raw
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    public record ExportFile(String fileName, String contentType, byte[] content) {
    }

}