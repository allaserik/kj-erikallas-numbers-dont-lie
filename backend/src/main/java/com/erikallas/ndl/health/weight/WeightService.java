package com.erikallas.ndl.health.weight;

import com.erikallas.ndl.health.profile.HealthProfileRepository;
import com.erikallas.ndl.health.wellness.WellnessScoreService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeightService {

    private final WeightEntryRepository repo;
    private final HealthProfileRepository profileRepo;
    private final WellnessScoreService wellnessScoreService;

    public WeightService(WeightEntryRepository repo, HealthProfileRepository profileRepo,
            WellnessScoreService wellnessScoreService) {
        this.repo = repo;
        this.profileRepo = profileRepo;
        this.wellnessScoreService = wellnessScoreService;
    }

    @Transactional
    public WeightEntryEntity add(UUID userId, double weightKg, OffsetDateTime measuredAt, String note) {
        // Save weight entry
        var entry = repo.save(new WeightEntryEntity(UUID.randomUUID(), userId,
                measuredAt != null ? measuredAt : OffsetDateTime.now(), weightKg, note));

        // Update BMI in health profile if it exists
        // Just a note on using var vs explicit types:
        // I prefer explicit types for method signatures and
        // public APIs for clarity, but use var for local variables
        // to reduce boilerplate and improve readability.
        // Note that we are checking for presence before calling get().
        var profile = profileRepo.findById(userId);
        if (profile.isPresent()) {
            var healthProfile = profile.get();
            healthProfile.calculateBMI(weightKg);
            healthProfile.setUpdatedAt(OffsetDateTime.now());
            profileRepo.save(healthProfile);

            // Auto-calculate wellness score after BMI update
            wellnessScoreService.calculateAndUpdateWellnessScore(userId);
        }

        return entry;
    }

    public List<WeightEntryEntity> latest(UUID userId) {
        return repo.findTop30ByUserIdOrderByMeasuredAtDesc(userId);
    }
}
