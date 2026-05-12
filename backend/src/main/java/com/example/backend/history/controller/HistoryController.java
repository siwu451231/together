package com.example.backend.history.controller;

import com.example.backend.collab.entity.DocumentOperation;
import com.example.backend.common.response.Result;
import com.example.backend.common.util.SecurityUtils;
import com.example.backend.document.dto.DocumentResponse;
import com.example.backend.history.service.HistoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {
    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/{documentId}")
    public Result<List<DocumentOperation>> list(@PathVariable Long documentId) {
        return Result.ok(historyService.list(SecurityUtils.currentUserId(), documentId));
    }

    @PostMapping("/{documentId}/rollback/{operationId}")
    public Result<DocumentResponse> rollback(@PathVariable Long documentId, @PathVariable Long operationId) {
        return Result.ok(historyService.rollback(SecurityUtils.currentUserId(), documentId, operationId));
    }
}
