package com.erikallas.ndl.nutrition.preferences;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface UserDietaryPreferenceRepository
        extends JpaRepository<UserDietaryPreferenceEntity, UserDietaryPreferenceId> {
    List<UserDietaryPreferenceEntity> findByIdUserId(UUID userId);

    @Transactional
    void deleteByIdUserId(UUID userId);
}
