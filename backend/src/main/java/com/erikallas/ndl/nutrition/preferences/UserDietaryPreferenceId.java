package com.erikallas.ndl.nutrition.preferences;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class UserDietaryPreferenceId implements Serializable {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "preference_code")
    private String preferenceCode;

    protected UserDietaryPreferenceId() {
    }

    public UserDietaryPreferenceId(UUID userId, String preferenceCode) {
        this.userId = userId;
        this.preferenceCode = preferenceCode;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getPreferenceCode() {
        return preferenceCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserDietaryPreferenceId that)) {
            return false;
        }
        return Objects.equals(userId, that.userId) && Objects.equals(preferenceCode, that.preferenceCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, preferenceCode);
    }
}
