package com.erikallas.ndl.nutrition.preferences;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_dietary_preferences")
public class UserDietaryPreferenceEntity {

    @EmbeddedId
    private UserDietaryPreferenceId id;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected UserDietaryPreferenceEntity() {
    }

    public UserDietaryPreferenceEntity(UserDietaryPreferenceId id, OffsetDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public UserDietaryPreferenceId getId() {
        return id;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
