package com.example.backend.collab.ws;

import com.example.backend.collab.dto.CollabBroadcastMessage;
import com.example.backend.collab.dto.CollabChatBroadcastMessage;
import com.example.backend.collab.dto.CollabChatMessage;
import com.example.backend.collab.dto.CollabEditMessage;
import com.example.backend.collab.dto.CollabPresenceBroadcastMessage;
import com.example.backend.collab.dto.CollabPresenceMessage;
import com.example.backend.collab.service.CollabService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class CollabWebSocketController {
    private final CollabService collabService;
    private final SimpMessagingTemplate messagingTemplate;

    public CollabWebSocketController(CollabService collabService, SimpMessagingTemplate messagingTemplate) {
        this.collabService = collabService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/collab.edit")
    public void handleEdit(CollabEditMessage message) {
        CollabBroadcastMessage broadcastMessage = collabService.applyEdit(message);
        messagingTemplate.convertAndSend("/topic/document." + message.getDocumentId(), broadcastMessage);
    }

    @MessageMapping("/collab.presence")
    public void handlePresence(CollabPresenceMessage message) {
        CollabPresenceBroadcastMessage broadcastMessage = collabService.applyPresence(message);
        messagingTemplate.convertAndSend("/topic/document." + message.getDocumentId() + ".presence", broadcastMessage);
    }

    @MessageMapping("/collab.chat")
    public void handleChat(CollabChatMessage message) {
        CollabChatBroadcastMessage broadcastMessage = collabService.applyChat(message);
        messagingTemplate.convertAndSend("/topic/document." + message.getDocumentId() + ".chat", broadcastMessage);
    }
}
