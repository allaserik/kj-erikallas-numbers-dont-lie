package com.erikallas.ndl.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * User entity for authentication.
 * 
 * Represents a registered user with email/password authentication. Password is
 * hashed using BCrypt and never stored in plain text.
 * 
 * Fields: - id: Unique identifier (UUID) - email: Unique email address
 * (lowercase) - passwordHash: BCrypt-hashed password - emailVerified: True
 * after user enters verification code - createdAt: Registration timestamp -
 * updatedAt: Last update timestamp
 */
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Constructors

    protected UserEntity() {
        // JPA requires no-arg constructor
    }

    public UserEntity(UUID id, String email, String passwordHash) {
        this.id = id;
        this.email = email.toLowerCase(); // Normalize email
        this.passwordHash = passwordHash;
        this.emailVerified = false;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        this.updatedAt = OffsetDateTime.now();
    }

    public void setEmailVerified(Boolean verified) {
        this.emailVerified = verified;
        this.updatedAt = OffsetDateTime.now();
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
