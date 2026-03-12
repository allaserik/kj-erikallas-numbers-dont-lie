package com.erikallas.ndl.nutrition.preferences;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dietary_preference_options")
public class DietaryPreferenceOptionEntity {

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    protected DietaryPreferenceOptionEntity() {
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isActive() {
        return active;
    }
}
