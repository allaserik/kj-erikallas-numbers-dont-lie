package com.erikallas.ndl.nutrition.preferences;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class UserAllergyId implements Serializable {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "allergy_code")
    private String allergyCode;

    protected UserAllergyId() {
    }

    public UserAllergyId(UUID userId, String allergyCode) {
        this.userId = userId;
        this.allergyCode = allergyCode;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getAllergyCode() {
        return allergyCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserAllergyId that)) {
            return false;
        }
        return Objects.equals(userId, that.userId) && Objects.equals(allergyCode, that.allergyCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, allergyCode);
    }
}
