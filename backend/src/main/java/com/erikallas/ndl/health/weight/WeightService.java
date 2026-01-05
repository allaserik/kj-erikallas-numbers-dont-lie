package com.erikallas.ndl.health.weight;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class WeightService {

    private final WeightEntryRepository repo;

    public WeightService(WeightEntryRepository repo) {
        this.repo = repo;
    }

    public WeightEntryEntity add(UUID userId, double weightKg, OffsetDateTime measuredAt, String note) {
        return repo.save(new WeightEntryEntity(
                UUID.randomUUID(),
                userId,
                measuredAt != null ? measuredAt : OffsetDateTime.now(),
                weightKg,
                note));
    }

    public List<WeightEntryEntity> latest(UUID userId) {
        return repo.findTop30ByUserIdOrderByMeasuredAtDesc(userId);
    }
}
