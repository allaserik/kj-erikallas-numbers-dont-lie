package com.erikallas.ndl.privacy;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivacyPreferencesRepository extends JpaRepository<PrivacyPreferencesEntity, UUID> {
    Optional<PrivacyPreferencesEntity> findByUserId(UUID userId);
}
