package com.example.backend.auth.controller;

import com.example.backend.auth.dto.AuthResponse;
import com.example.backend.auth.dto.LoginRequest;
import com.example.backend.auth.dto.RegisterRequest;
import com.example.backend.auth.repository.UserRepository;
import com.example.backend.auth.service.AuthService;
import com.example.backend.common.response.Result;
import com.example.backend.common.util.SecurityUtils;
import com.example.backend.user.entity.User;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(authService.register(request));
    }

    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @GetMapping("/me")
    public Result<UserInfo> me() {
        Long userId = SecurityUtils.currentUserId();
        User user = userRepository.findById(userId).orElseThrow();
        return Result.ok(new UserInfo(user.getId(), user.getUsername(), user.getDisplayName(), user.getEmail()));
    }

    private static class UserInfo {
        private final Long id;
        private final String username;
        private final String displayName;
        private final String email;

        private UserInfo(Long id, String username, String displayName, String email) {
            this.id = id;
            this.username = username;
            this.displayName = displayName;
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getEmail() {
            return email;
        }
    }
}