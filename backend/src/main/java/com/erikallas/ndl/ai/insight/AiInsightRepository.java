package com.erikallas.ndl.ai.insight;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiInsightRepository extends JpaRepository<AiInsightEntity, UUID> {
    Optional<AiInsightEntity> findFirstByUserIdAndInputHashOrderByCreatedAtDesc(UUID userId, String inputHash);

    Optional<AiInsightEntity> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);
}
