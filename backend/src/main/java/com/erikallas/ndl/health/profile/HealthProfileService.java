package com.erikallas.ndl.health.profile;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HealthProfileService {

    private final HealthProfileRepository repo;

    public HealthProfileService(HealthProfileRepository repo) {
        this.repo = repo;
    }

    public Optional<HealthProfileEntity> find(UUID userId) {
        return repo.findById(userId);
    }

    @Transactional
    public HealthProfileEntity upsert(
            UUID userId,
            Integer birthYear,
            String gender,
            int heightCm,
            String baselineActivityLevel) {
        OffsetDateTime now = OffsetDateTime.now();
        HealthProfileEntity existing = repo.findById(userId).orElse(null);
        OffsetDateTime createdAt = existing == null ? now : existing.getCreatedAt();

        return repo.save(new HealthProfileEntity(
                userId,
                birthYear,
                gender,
                heightCm,
                baselineActivityLevel,
                createdAt,
                now));
    }
}
