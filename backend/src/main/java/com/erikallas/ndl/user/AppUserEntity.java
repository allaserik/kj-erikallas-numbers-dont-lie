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

    @Column(name = "auth0_sub", nullable = false, unique = true)
    private String auth0Sub;

    @Column(name = "email")
    private String email;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected AppUserEntity() {
    }

    public AppUserEntity(UUID id, String auth0Sub, String email, OffsetDateTime createdAt) {
        this.id = id;
        this.auth0Sub = auth0Sub;
        this.email = email;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getAuth0Sub() {
        return auth0Sub;
    }

    public String getEmail() {
        return email;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
