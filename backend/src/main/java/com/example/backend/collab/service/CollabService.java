package com.example.backend.collab.service;

import com.example.backend.auth.repository.UserRepository;
import com.example.backend.collab.dto.CollabBroadcastMessage;
import com.example.backend.collab.dto.CollabChatBroadcastMessage;
import com.example.backend.collab.dto.CollabChatMessage;
import com.example.backend.collab.dto.CollabEditMessage;
import com.example.backend.collab.dto.CollabPresenceBroadcastMessage;
import com.example.backend.collab.dto.CollabPresenceMessage;
import com.example.backend.common.config.JwtService;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.document.entity.Document;
import com.example.backend.document.entity.MemberRole;
import com.example.backend.document.repository.DocumentRepository;
import com.example.backend.document.service.DocumentService;
import com.example.backend.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CollabService {
    private final DocumentService documentService;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final Map<Long, Map<Long, PresenceUser>> onlineUsersByDoc = new ConcurrentHashMap<>();

    public CollabService(DocumentService documentService,
                         DocumentRepository documentRepository,
                         UserRepository userRepository,
                         JwtService jwtService) {
        this.documentService = documentService;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public CollabBroadcastMessage applyEdit(CollabEditMessage message) {
        if (message.getDocumentId() == null) {
            throw new BusinessException("documentId 不能为空");
        }
        if (message.getToken() == null || message.getToken().isBlank() || !jwtService.isTokenValid(message.getToken())) {
            throw new BusinessException(401, "未授权的协作请求");
        }

        String username = jwtService.extractUsername(message.getToken());
        User sender = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        Long userId = sender.getId();

        MemberRole role = documentService.getRole(userId, message.getDocumentId());
        if (role == null) {
            throw new BusinessException(403, "无编辑权限");
        }

        Document doc = documentService.getDocument(message.getDocumentId());
        String draftContent = message.getContent() == null ? "" : message.getContent();
        doc.setContent(draftContent);
        documentRepository.save(doc);

        return new CollabBroadcastMessage(
                message.getDocumentId(),
                doc.getVersion(),
                draftContent,
                userId,
                sender.getDisplayName(),
                LocalDateTime.now()
        );
    }

    public CollabPresenceBroadcastMessage applyPresence(CollabPresenceMessage message) {
        if (message.getDocumentId() == null) {
            throw new BusinessException("documentId 不能为空");
        }
        if (message.getToken() == null || message.getToken().isBlank() || !jwtService.isTokenValid(message.getToken())) {
            throw new BusinessException(401, "未授权的协作请求");
        }

        String username = jwtService.extractUsername(message.getToken());
        var user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));

        MemberRole role = documentService.getRole(user.getId(), message.getDocumentId());
        if (role == null) {
            throw new BusinessException(403, "无访问权限");
        }

        String action = message.getAction() == null ? "join" : message.getAction().toLowerCase();
        String clientId = (message.getClientId() == null || message.getClientId().isBlank())
                ? "default"
                : message.getClientId();

        if ("leave".equals(action)) {
            removePresence(message.getDocumentId(), user.getId(), clientId);
        } else {
            addPresence(message.getDocumentId(), user.getId(), user.getDisplayName(), clientId, message.getColor());
        }

        return buildPresenceSnapshot(message.getDocumentId());
    }

    public CollabChatBroadcastMessage applyChat(CollabChatMessage message) {
        if (message.getDocumentId() == null) {
            throw new BusinessException("documentId 不能为空");
        }
        if (message.getToken() == null || message.getToken().isBlank() || !jwtService.isTokenValid(message.getToken())) {
            throw new BusinessException(401, "未授权的聊天请求");
        }

        String username = jwtService.extractUsername(message.getToken());
        User sender = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));

        MemberRole role = documentService.getRole(sender.getId(), message.getDocumentId());
        if (role == null) {
            throw new BusinessException(403, "无访问权限");
        }

        String raw = message.getContent() == null ? "" : message.getContent();
        String content = raw.trim();
        if (content.isEmpty()) {
            throw new BusinessException(400, "消息不能为空");
        }
        if (content.length() > 500) {
            throw new BusinessException(400, "消息长度不能超过500字符");
        }

        String displayName = sender.getDisplayName() == null || sender.getDisplayName().isBlank()
                ? sender.getUsername()
                : sender.getDisplayName();

        return new CollabChatBroadcastMessage(
                message.getDocumentId(),
                sender.getId(),
                displayName,
                content,
                message.getColor(),
                LocalDateTime.now()
        );
    }

    private void addPresence(Long documentId, Long userId, String displayName, String clientId, String color) {
        onlineUsersByDoc.computeIfAbsent(documentId, k -> new ConcurrentHashMap<>());
        Map<Long, PresenceUser> users = onlineUsersByDoc.get(documentId);
        users.computeIfAbsent(userId, k -> new PresenceUser(userId, displayName, color != null ? color : "#cccccc"));
        // 如果用户重新连接或更新颜色，更新color
        PresenceUser existingUser = users.get(userId);
        if (color != null && !color.equals(existingUser.color())) {
             existingUser = new PresenceUser(existingUser.userId(), existingUser.displayName(), existingUser.clientIds(), color);
             users.put(userId, existingUser);
        }
        existingUser.clientIds().add(clientId);
    }

    private void removePresence(Long documentId, Long userId, String clientId) {
        Map<Long, PresenceUser> users = onlineUsersByDoc.get(documentId);
        if (users == null) {
            return;
        }

        PresenceUser presenceUser = users.get(userId);
        if (presenceUser == null) {
            return;
        }

        presenceUser.clientIds().remove(clientId);
        if (presenceUser.clientIds().isEmpty()) {
            users.remove(userId);
        }

        if (users.isEmpty()) {
            onlineUsersByDoc.remove(documentId);
        }
    }

    private CollabPresenceBroadcastMessage buildPresenceSnapshot(Long documentId) {
        Map<Long, PresenceUser> users = onlineUsersByDoc.get(documentId);
        if (users == null || users.isEmpty()) {
            return new CollabPresenceBroadcastMessage(documentId, 0, new ArrayList<>(), LocalDateTime.now());
        }

        var userList = users.values().stream()
                .sorted(Comparator.comparing(PresenceUser::displayName, Comparator.nullsLast(String::compareTo)))
                .map(u -> new CollabPresenceBroadcastMessage.OnlineUser(u.userId(), u.displayName(), u.color()))
                .toList();

        return new CollabPresenceBroadcastMessage(documentId, userList.size(), userList, LocalDateTime.now());
    }

    private record PresenceUser(Long userId, String displayName, Set<String> clientIds, String color) {
        private PresenceUser(Long userId, String displayName, String color) {
            this(userId, displayName == null || displayName.isBlank() ? "匿名用户" : displayName, ConcurrentHashMap.newKeySet(), color);
        }
    }
}