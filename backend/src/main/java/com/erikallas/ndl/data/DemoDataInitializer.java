package com.erikallas.ndl.data;

import com.erikallas.ndl.auth.user.model.UserEntity;
import com.erikallas.ndl.auth.user.model.UserRepository;
import com.erikallas.ndl.health.goal.GoalEntity;
import com.erikallas.ndl.health.goal.GoalRepository;
import com.erikallas.ndl.health.goal.GoalType;
import com.erikallas.ndl.health.profile.HealthProfileEntity;
import com.erikallas.ndl.health.profile.HealthProfileRepository;
import com.erikallas.ndl.health.weight.WeightEntryEntity;
import com.erikallas.ndl.health.weight.WeightEntryRepository;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initializes demo data for testing and visualization.
 * Activated when demo.mode=true
 */
@Component
@ConditionalOnProperty(name = "demo.mode", havingValue = "true")
public class DemoDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);
    private static final String DEMO_EMAIL = "demo@example.com";
    private static final UUID DEMO_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final UserRepository userRepo;
    private final HealthProfileRepository profileRepo;
    private final GoalRepository goalRepo;
    private final WeightEntryRepository weightRepo;
    private final PasswordEncoder passwordEncoder;

    public DemoDataInitializer(UserRepository userRepo, HealthProfileRepository profileRepo, GoalRepository goalRepo,
            WeightEntryRepository weightRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.goalRepo = goalRepo;
        this.weightRepo = weightRepo;
        this.passwordEncoder = passwordEncoder;
        initializeDemo();
    }

    @Transactional
    private void initializeDemo() {
        try {
            // Check if demo user already exists
            if (userRepo.findById(DEMO_USER_ID).isPresent()) {
                log.info("Demo user already exists, skipping initialization");
                return;
            }

            log.info("Initializing demo data...");

            // 1. Create demo user with email/password (NOT Auth0)
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            UserEntity user = new UserEntity(DEMO_USER_ID, null, DEMO_EMAIL, now);
            // Set password: demo@example.com (encoded)
            user.setPasswordHash(passwordEncoder.encode(DEMO_EMAIL));
            user.setEmailVerified(true);
            userRepo.save(user);
            log.info("Created demo user: {} (email/password auth)", DEMO_EMAIL);

            // 2. Create health profile
            HealthProfileEntity profile = new HealthProfileEntity(
                    DEMO_USER_ID, 1990, "Other", 175, "MODERATE", now, now
            );
            profile.setDietaryPreferences(List.of("Vegetarian", "Low-sugar"));
            profile.setDietaryRestrictions(List.of());
            profile.setFitnessAssessmentCompleted(true);
            profileRepo.save(profile);
            log.info("Created demo health profile (height: 175cm)");

            // 3. Create active weight loss goal
            GoalEntity goal = new GoalEntity(UUID.randomUUID(), DEMO_USER_ID, GoalType.WEIGHT_LOSS, 75.0, 4, "Lose 5kg in 3 months", true,
                    now, now);
            goalRepo.save(goal);
            log.info("Created demo goal (WEIGHT_LOSS, target: 75kg)");

            // 4. Create 30 days of weight entries with realistic progression
            createWeightEntries(DEMO_USER_ID, now);
            log.info("Created 30 days of weight entries");

            log.info("Demo data initialization complete!");

        } catch (Exception e) {
            log.error("Failed to initialize demo data", e);
            throw new RuntimeException("Demo data initialization failed", e);
        }
    }

    private void createWeightEntries(UUID userId, OffsetDateTime baseTime) {
        // Generate 30 days of weight data, trending downward (weight loss)
        // Start from 30 days ago, daily measurements
        double startWeight = 82.5;
        double dailyVariance = 0.3; // kg of daily fluctuation

        for (int daysAgo = 29; daysAgo >= 0; daysAgo--) {
            OffsetDateTime measuredAt = baseTime.minusDays(daysAgo);

            // Overall trend: losing ~0.15kg per day, plus random variation
            double trendDownward = (29 - daysAgo) * 0.15;
            double variance = (Math.random() - 0.5) * dailyVariance;
            double weight = startWeight - trendDownward + variance;

            WeightEntryEntity entry = new WeightEntryEntity(UUID.randomUUID(), userId, measuredAt, Math.round(weight * 100.0) / 100.0,
                    "Daily weigh-in");
            weightRepo.save(entry);
        }
    }
}
