package com.erikallas.ndl.health.weight;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeightEntryRepository extends JpaRepository<WeightEntryEntity, UUID> {
    List<WeightEntryEntity> findTop30ByUserIdOrderByMeasuredAtDesc(UUID userId);

    boolean existsByUserId(UUID userId);
}
