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
 * Email verification code entity.
 * 
 * When a user registers, a 6-digit code is generated and sent to their email.
 * User must enter this code within 24 hours to verify they own the email. Code
 * is one-time use: once verified_at is set, code cannot be reused.
 * 
 * Fields: - id: Unique identifier (UUID) - userId: Reference to users table -
 * code: 6-digit code sent to email (e.g., "123456") - createdAt: When code was
 * generated - expiresAt: When code expires (24 hours from creation) -
 * verifiedAt: When code was successfully used (NULL if not used) -
 * lastResentAt: Timestamp of last resend (for rate limiting)
 */
@Entity
@Table(name = "email_verification_codes")
public class EmailVerificationCodeEntity {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String code; // 6 digits: "123456"

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt; // created_at + 24 hours

    @Column(name = "verified_at")
    private OffsetDateTime verifiedAt; // NULL until verified

    @Column(name = "last_resent_at")
    private OffsetDateTime lastResentAt; // For rate limiting

    // Constructors

    protected EmailVerificationCodeEntity() {
        // JPA requires no-arg constructor
    }

    public EmailVerificationCodeEntity(UUID id, UserEntity user, String code, OffsetDateTime expiresAt) {
        this.id = id;
        this.user = user;
        this.code = code;
        this.createdAt = OffsetDateTime.now();
        this.expiresAt = expiresAt;
        this.verifiedAt = null;
        this.lastResentAt = null;
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public String getCode() {
        return code;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public OffsetDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public OffsetDateTime getLastResentAt() {
        return lastResentAt;
    }

    // Setters

    public void setVerifiedAt(OffsetDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public void setLastResentAt(OffsetDateTime lastResentAt) {
        this.lastResentAt = lastResentAt;
    }

    // Business Logic Methods

    /**
     * Check if code is expired (current time > expiresAt)
     */
    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if code has already been verified (verified_at is not null)
     */
    public boolean isAlreadyVerified() {
        return verifiedAt != null;
    }

    /**
     * Check if code can be resent (respects 1-minute rate limit) Can resend if: 1.
     * Never resent before (lastResentAt is null), OR 2. More than 1 minute has
     * passed since last resend
     */
    public boolean canResend() {
        if (lastResentAt == null) {
            return true; // First time, can always resend
        }
        OffsetDateTime oneMinuteAgo = OffsetDateTime.now().minusMinutes(1);
        return lastResentAt.isBefore(oneMinuteAgo);
    }

    /**
     * Mark this code as verified (one-time use)
     */
    public void markAsVerified() {
        this.verifiedAt = OffsetDateTime.now();
    }
}
