package com.erikallas.ndl.data;

import com.erikallas.ndl.auth.user.model.UserEntity;
import com.erikallas.ndl.auth.user.model.UserRepository;
import com.erikallas.ndl.health.goal.GoalEntity;
import com.erikallas.ndl.health.goal.GoalProgressEntity;
import com.erikallas.ndl.health.goal.GoalProgressRepository;
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
import java.util.Map;
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
    private final GoalProgressRepository goalProgressRepo;
    private final WeightEntryRepository weightRepo;
    private final PasswordEncoder passwordEncoder;

    public DemoDataInitializer(UserRepository userRepo, HealthProfileRepository profileRepo, GoalRepository goalRepo,
            GoalProgressRepository goalProgressRepo, WeightEntryRepository weightRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.goalRepo = goalRepo;
        this.goalProgressRepo = goalProgressRepo;
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
            profile.setDietaryPreferences(List.of("High-protein", "Low-sugar"));
            profile.setDietaryRestrictions(List.of("Lactose"));
            profile.setFitnessAssessment(Map.of(
                    "occupation_type", "Office",
                    "current_activity_frequency", 4,
                    "exercise_types", List.of("Cardio", "Strength", "Outdoors"),
                    "average_session_duration", "30_60",
                    "self_assessed_fitness_level", "INTERMEDIATE",
                    "preferred_exercise_environment", "GYM",
                    "exercise_time_preference", "EVENING",
                    "current_endurance_minutes", 35,
                    "pushups_count", 30,
                    "situps_count", 45,
                    "pullups_count", 8,
                    "run_3km_time_sec", 980
            ));
            profile.setFitnessAssessmentCompleted(true);
            profileRepo.save(profile);
            log.info("Created demo health profile (height: 175cm)");

            // 3. Create goals (one active, one archived for history testing)
            GoalEntity activeGoal = new GoalEntity(UUID.randomUUID(), DEMO_USER_ID, GoalType.WEIGHT_LOSS, 75.0, 4,
                    now.toLocalDate().plusDays(90), "Lose 5kg in 3 months", true,
                    now, now);
            goalRepo.save(activeGoal);

            GoalEntity archivedGoal = new GoalEntity(UUID.randomUUID(), DEMO_USER_ID, GoalType.IMPROVE_FITNESS, null,
                    5, now.toLocalDate().minusDays(10), "Improve weekly training consistency", false,
                    now.minusDays(120), now.minusDays(20));
            goalRepo.save(archivedGoal);
            log.info("Created demo goals (active + archived)");

            // 4. Create 30 days of weight entries with realistic progression
            createWeightEntries(DEMO_USER_ID, now);
            log.info("Created 30 days of weight entries");

            // 5. Create goal progress history for trend charts and analytics
            createGoalProgressHistory(activeGoal, now);
            createArchivedGoalProgressHistory(archivedGoal, now);
            log.info("Created goal progress history");

            // 6. Set BMI and wellness score snapshot for faster first-load demo UX
            var latestWeight = weightRepo.findTop30ByUserIdOrderByMeasuredAtDesc(DEMO_USER_ID).stream().findFirst();
            latestWeight.ifPresent(w -> profile.calculateBMI(w.getWeightKg()));
            profile.setWellnessScore(74);
            profile.setUpdatedAt(now);
            profileRepo.save(profile);

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

    private void createGoalProgressHistory(GoalEntity goal, OffsetDateTime baseTime) {
        // Progress snapshots over ~8 weeks to support weekly/monthly summary testing.
        double[] currentWeights = { 82.1, 81.5, 80.9, 80.4, 79.8, 79.1, 78.4, 77.8, 77.2 };
        int[] progressPercents = { 8, 16, 24, 33, 45, 57, 68, 79, 88 };

        for (int i = 0; i < currentWeights.length; i++) {
            OffsetDateTime ts = baseTime.minusDays(56 - (i * 7));
            GoalProgressEntity progress = new GoalProgressEntity(
                    UUID.randomUUID(),
                    goal.getId(),
                    goal.getUserId(),
                    java.math.BigDecimal.valueOf(currentWeights[i]),
                    progressPercents[i],
                    true,
                    Math.max(0, (int) java.time.temporal.ChronoUnit.DAYS.between(baseTime, goal.getTargetDate().atStartOfDay().atOffset(java.time.ZoneOffset.UTC))),
                    ts,
                    ts,
                    ts
            );
            progress.setMilestonesCompleted(progressPercents[i] / 5);
            progress.setMilestoneDetails(List.of(Map.of(
                    "percentage", (progressPercents[i] / 5) * 5,
                    "completed_at", ts.toString()
            )));
            goalProgressRepo.save(progress);
        }
    }

    private void createArchivedGoalProgressHistory(GoalEntity goal, OffsetDateTime baseTime) {
        int[] activityDays = { 2, 3, 3, 4, 5 };
        int[] progressPercents = { 40, 55, 60, 75, 100 };

        for (int i = 0; i < activityDays.length; i++) {
            OffsetDateTime ts = baseTime.minusDays(110 - (i * 10));
            GoalProgressEntity progress = new GoalProgressEntity(
                    UUID.randomUUID(),
                    goal.getId(),
                    goal.getUserId(),
                    java.math.BigDecimal.valueOf(activityDays[i]),
                    progressPercents[i],
                    true,
                    0,
                    ts,
                    ts,
                    ts
            );
            progress.setMilestonesCompleted(progressPercents[i] / 5);
            progress.setMilestoneDetails(List.of(Map.of(
                    "percentage", (progressPercents[i] / 5) * 5,
                    "completed_at", ts.toString()
            )));
            goalProgressRepo.save(progress);
        }
    }
}
