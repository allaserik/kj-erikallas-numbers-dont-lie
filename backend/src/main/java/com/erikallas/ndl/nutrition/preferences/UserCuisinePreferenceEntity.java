package com.erikallas.ndl.nutrition.preferences;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_cuisine_preferences")
public class UserCuisinePreferenceEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "cuisine_label", nullable = false)
    private String cuisineLabel;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected UserCuisinePreferenceEntity() {
    }

    public UserCuisinePreferenceEntity(UUID id, UUID userId, String cuisineLabel, OffsetDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.cuisineLabel = cuisineLabel;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getCuisineLabel() {
        return cuisineLabel;
    }
}
