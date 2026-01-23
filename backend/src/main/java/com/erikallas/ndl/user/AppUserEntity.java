package com.erikallas.ndl.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class AppUserEntity {

    @Id
    private UUID id;

    @Column(name = "auth0_sub", unique = true)
    private String auth0Sub;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    protected AppUserEntity() {
    }

    public AppUserEntity(UUID id, String auth0Sub, String email, OffsetDateTime createdAt) {
        this.id = id;
        this.auth0Sub = auth0Sub;
        this.email = email;
        this.createdAt = createdAt;
        this.emailVerified = false;
    }

    public UUID getId() {
        return id;
    }

    public String getAuth0Sub() {
        return auth0Sub;
    }

    public void setAuth0Sub(String auth0Sub) {
        this.auth0Sub = auth0Sub;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public boolean isAuth0User() {
        return auth0Sub != null;
    }

    public boolean isEmailPasswordUser() {
        return email != null && passwordHash != null;
    }
}
