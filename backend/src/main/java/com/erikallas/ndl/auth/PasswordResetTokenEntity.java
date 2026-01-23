package com.erikallas.ndl.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Password reset token entity.
 * 
 * When a user requests password reset, a random token is generated and sent to
 * their email. User clicks link with token, which takes them to reset form
 * where they set a new password. Token is one-time use: once used_at is set,
 * token cannot be reused. Token is valid for 1 hour.
 * 
 * Fields: - id: Unique identifier (UUID) - userId: Reference to users table -
 * token: Random token sent in email (e.g., UUID as string) - createdAt: When
 * token was generated - expiresAt: When token expires (1 hour from creation) -
 * usedAt: When token was successfully used to reset password (NULL if not used)
 */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetTokenEntity {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, unique = true)
    private String token; // Random token, typically a UUID string

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt; // created_at + 1 hour

    @Column(name = "used_at")
    private OffsetDateTime usedAt; // NULL until token is used

    // Constructors

    protected PasswordResetTokenEntity() {
        // JPA requires no-arg constructor
    }

    public PasswordResetTokenEntity(UUID id, UserEntity user, String token, OffsetDateTime expiresAt) {
        this.id = id;
        this.user = user;
        this.token = token;
        this.createdAt = OffsetDateTime.now();
        this.expiresAt = expiresAt;
        this.usedAt = null;
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public OffsetDateTime getUsedAt() {
        return usedAt;
    }

    // Setters

    public void setUsedAt(OffsetDateTime usedAt) {
        this.usedAt = usedAt;
    }

    // Business Logic Methods

    /**
     * Check if token is expired (current time > expiresAt)
     */
    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if token has already been used (usedAt is not null) One-time use:
     * cannot be used twice
     */
    public boolean isAlreadyUsed() {
        return usedAt != null;
    }

    /**
     * Check if token is valid (not expired and not already used)
     */
    public boolean isValid() {
        return !isExpired() && !isAlreadyUsed();
    }

    /**
     * Mark this token as used (one-time use)
     */
    public void markAsUsed() {
        this.usedAt = OffsetDateTime.now();
    }
}
