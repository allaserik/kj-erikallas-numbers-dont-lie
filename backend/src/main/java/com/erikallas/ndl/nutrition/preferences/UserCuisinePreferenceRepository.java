package com.erikallas.ndl.nutrition.preferences;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface UserCuisinePreferenceRepository extends JpaRepository<UserCuisinePreferenceEntity, UUID> {
    List<UserCuisinePreferenceEntity> findByUserId(UUID userId);

    @Transactional
    void deleteByUserId(UUID userId);
}
