package com.erikallas.ndl.nutrition.preferences;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_disliked_ingredients")
public class UserDislikedIngredientEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "ingredient_label", nullable = false)
    private String ingredientLabel;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected UserDislikedIngredientEntity() {
    }

    public UserDislikedIngredientEntity(UUID id, UUID userId, String ingredientLabel, OffsetDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.ingredientLabel = ingredientLabel;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getIngredientLabel() {
        return ingredientLabel;
    }
}
