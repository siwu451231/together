package com.example.backend.user.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String password;

    @Column(name = "password", nullable = false, length = 100)
    private String legacyPassword;

    @Column(nullable = false, length = 60)
    private String displayName;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (password == null && legacyPassword != null) {
            password = legacyPassword;
        }
        if (legacyPassword == null && password != null) {
            legacyPassword = password;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password != null ? password : legacyPassword;
    }

    public void setPassword(String password) {
        this.password = password;
        this.legacyPassword = password;
    }

    public String getLegacyPassword() {
        return legacyPassword;
    }

    public void setLegacyPassword(String legacyPassword) {
        this.legacyPassword = legacyPassword;
        if (this.password == null) {
            this.password = legacyPassword;
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
