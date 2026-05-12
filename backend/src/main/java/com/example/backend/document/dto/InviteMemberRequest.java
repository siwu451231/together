package com.example.backend.document.dto;

import com.example.backend.document.entity.MemberRole;
import jakarta.validation.constraints.NotBlank;

public class InviteMemberRequest {
    @NotBlank
    private String username;

    private MemberRole role = MemberRole.EDITOR;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public MemberRole getRole() {
        return role;
    }

    public void setRole(MemberRole role) {
        this.role = role;
    }
}
