package com.erikallas.ndl.nutrition.preferences;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_allergies")
public class UserAllergyEntity {

    @EmbeddedId
    private UserAllergyId id;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected UserAllergyEntity() {
    }

    public UserAllergyEntity(UserAllergyId id, OffsetDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public UserAllergyId getId() {
        return id;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
