package com.erikallas.ndl.health.weight;

import com.erikallas.ndl.health.profile.HealthProfileRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeightService {

    private final WeightEntryRepository repo;
    private final HealthProfileRepository profileRepo;

    public WeightService(WeightEntryRepository repo, HealthProfileRepository profileRepo) {
        this.repo = repo;
        this.profileRepo = profileRepo;
    }

    @Transactional
    public WeightEntryEntity add(UUID userId, double weightKg, OffsetDateTime measuredAt, String note) {
        // Save weight entry
        var entry = repo.save(new WeightEntryEntity(UUID.randomUUID(), userId,
                measuredAt != null ? measuredAt : OffsetDateTime.now(), weightKg, note));

        // Update BMI in health profile if it exists
        var profile = profileRepo.findById(userId);
        if (profile.isPresent()) {
            var healthProfile = profile.get();
            healthProfile.calculateBMI(weightKg);
            healthProfile.setUpdatedAt(OffsetDateTime.now());
            profileRepo.save(healthProfile);
        }

        return entry;
    }

    public List<WeightEntryEntity> latest(UUID userId) {
        return repo.findTop30ByUserIdOrderByMeasuredAtDesc(userId);
    }
}
