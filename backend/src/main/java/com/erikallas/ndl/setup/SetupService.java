package com.erikallas.ndl.setup;

import com.erikallas.ndl.health.goal.GoalRepository;
import com.erikallas.ndl.health.profile.HealthProfileRepository;
import com.erikallas.ndl.health.weight.WeightEntryRepository;
import com.erikallas.ndl.user.AppUserEntity;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Service that determines what parts of onboarding are complete.
 *
 * This is the "source of truth" for setup requirements. Add new requirements
 * here and they automatically propagate to the API.
 */
@Service
public class SetupService {

    private final HealthProfileRepository profileRepo;
    private final GoalRepository goalRepo;
    private final WeightEntryRepository weightRepo;

    public SetupService(HealthProfileRepository profileRepo, GoalRepository goalRepo,
            WeightEntryRepository weightRepo) {
        this.profileRepo = profileRepo;
        this.goalRepo = goalRepo;
        this.weightRepo = weightRepo;
    }

    /**
     * Check if a user has completed all setup requirements.
     *
     * Requirements: 1. Health profile (height, gender, etc.) 2. Active goal (weight
     * target) 3. At least one weight entry (baseline measurement)
     */
    public SetupStatusDto getSetupStatus(AppUserEntity user) {
        List<String> missing = new ArrayList<>();

        // Check profile exists
        if (profileRepo.findByUserId(user.getId()).isEmpty()) {
            missing.add("profile");
        }

        // Check active goal exists
        if (goalRepo.findActiveByUserId(user.getId()).isEmpty()) {
            missing.add("goal");
        }

        // Check at least one weight entry exists
        if (!weightRepo.existsByUserId(user.getId())) {
            missing.add("weight");
        }

        boolean isComplete = missing.isEmpty();
        return new SetupStatusDto(isComplete, missing);
    }
}
