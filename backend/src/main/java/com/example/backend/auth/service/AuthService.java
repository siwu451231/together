package com.example.backend.auth.service;

import com.example.backend.auth.dto.AuthResponse;
import com.example.backend.auth.dto.LoginRequest;
import com.example.backend.auth.dto.RegisterRequest;
import com.example.backend.auth.repository.UserRepository;
import com.example.backend.common.config.JwtService;
import com.example.backend.common.config.UserPrincipal;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.user.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setDisplayName(request.getDisplayName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user = userRepository.save(user);

        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtService.generateToken(principal);
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getDisplayName());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        String token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getDisplayName());
    }
}