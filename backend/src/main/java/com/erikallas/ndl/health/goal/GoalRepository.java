package com.erikallas.ndl.health.goal;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalRepository extends JpaRepository<GoalEntity, UUID> {
    Optional<GoalEntity> findFirstByUserIdAndActiveTrue(UUID userId);
}
