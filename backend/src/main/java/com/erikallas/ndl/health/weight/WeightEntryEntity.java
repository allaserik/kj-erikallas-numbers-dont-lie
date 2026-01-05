package com.erikallas.ndl.health.weight;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "weight_entries")
public class WeightEntryEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "measured_at", nullable = false)
    private OffsetDateTime measuredAt;

    @Column(name = "weight_kg", nullable = false)
    private double weightKg;

    @Column(name = "note")
    private String note;

    protected WeightEntryEntity() {
    }

    public WeightEntryEntity(UUID id, UUID userId, OffsetDateTime measuredAt, double weightKg, String note) {
        this.id = id;
        this.userId = userId;
        this.measuredAt = measuredAt;
        this.weightKg = weightKg;
        this.note = note;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public OffsetDateTime getMeasuredAt() {
        return measuredAt;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public String getNote() {
        return note;
    }
}
