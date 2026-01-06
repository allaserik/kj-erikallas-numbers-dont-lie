package com.erikallas.ndl.ai.insight;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_insights")
public class AiInsightEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "goal_id")
    private UUID goalId;

    @Column(name = "input_hash", nullable = false)
    private String inputHash;

    @Column(name = "model", nullable = false)
    private String model;

    // Store JSON as string; DB column is jsonb
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected AiInsightEntity() {
    }

    public AiInsightEntity(UUID id, UUID userId, UUID goalId, String inputHash, String model, String payload,
            OffsetDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.goalId = goalId;
        this.inputHash = inputHash;
        this.model = model;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getGoalId() {
        return goalId;
    }

    public String getInputHash() {
        return inputHash;
    }

    public String getModel() {
        return model;
    }

    public String getPayload() {
        return payload;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
