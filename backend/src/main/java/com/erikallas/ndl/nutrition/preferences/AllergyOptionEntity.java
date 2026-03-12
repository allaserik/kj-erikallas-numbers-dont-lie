package com.erikallas.ndl.nutrition.preferences;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "allergy_options")
public class AllergyOptionEntity {

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    protected AllergyOptionEntity() {
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
