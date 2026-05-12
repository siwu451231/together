package com.example.backend.document.controller;

import com.example.backend.common.response.Result;
import com.example.backend.common.util.SecurityUtils;
import com.example.backend.document.dto.DocumentResponse;
import com.example.backend.document.dto.InvitationResponse;
import com.example.backend.document.service.InvitationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {
    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @GetMapping("/pending")
    public Result<List<InvitationResponse>> pending() {
        return Result.ok(invitationService.listPending(SecurityUtils.currentUserId()));
    }

    @PostMapping("/{id}/accept")
    public Result<DocumentResponse> accept(@PathVariable Long id) {
        return Result.ok(invitationService.accept(SecurityUtils.currentUserId(), id));
    }

    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id) {
        invitationService.reject(SecurityUtils.currentUserId(), id);
        return Result.ok("已拒绝邀请", null);
    }
}
