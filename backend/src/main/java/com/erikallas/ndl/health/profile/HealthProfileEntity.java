package com.erikallas.ndl.health.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "health_profiles")
public class HealthProfileEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "birth_year")
    private Integer birthYear;

    @Column(name = "gender")
    private String gender;

    @Column(name = "height_cm", nullable = false)
    private int heightCm;

    @Column(name = "baseline_activity_level", nullable = false)
    private String baselineActivityLevel;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected HealthProfileEntity() {
    }

    public HealthProfileEntity(
            UUID userId,
            Integer birthYear,
            String gender,
            int heightCm,
            String baselineActivityLevel,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {
        this.userId = userId;
        this.birthYear = birthYear;
        this.gender = gender;
        this.heightCm = heightCm;
        this.baselineActivityLevel = baselineActivityLevel;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getUserId() {
        return userId;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    public String getGender() {
        return gender;
    }

    public int getHeightCm() {
        return heightCm;
    }

    public String getBaselineActivityLevel() {
        return baselineActivityLevel;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
