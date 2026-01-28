package com.erikallas.ndl.ai.insight;

import com.erikallas.ndl.health.goal.GoalEntity;
import com.erikallas.ndl.health.goal.GoalProgressEntity;
import com.erikallas.ndl.health.goal.GoalProgressRepository;
import com.erikallas.ndl.health.goal.GoalRepository;
import com.erikallas.ndl.health.profile.HealthProfileEntity;
import com.erikallas.ndl.health.profile.HealthProfileService;
import com.erikallas.ndl.health.summary.HealthSummaryService;
import com.erikallas.ndl.health.weight.WeightEntryEntity;
import com.erikallas.ndl.health.weight.WeightEntryRepository;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Builder service for aggregating and formatting health data into structured
 * context for AI insight generation.
 * 
 * Collects: - User health profile (demographics, fitness, dietary) - Current
 * BMI and classification - Recent weight trends (7d, 30d, 90d) - Active goal
 * details and progress - Activity levels and compliance - Wellness metrics
 * 
 * Formats data as comprehensive prompt context for OpenAI, ensuring AI has
 * complete health picture for high-quality recommendations.
 */
@Service
public class InsightContextBuilder {

    private final HealthProfileService profileService;
    private final WeightEntryRepository weightRepo;
    private final GoalRepository goalRepo;
    private final GoalProgressRepository progressRepo;
    private final HealthSummaryService summaryService;

    public InsightContextBuilder(HealthProfileService profileService, WeightEntryRepository weightRepo,
            GoalRepository goalRepo, GoalProgressRepository progressRepo, HealthSummaryService summaryService) {
        this.profileService = profileService;
        this.weightRepo = weightRepo;
        this.goalRepo = goalRepo;
        this.progressRepo = progressRepo;
        this.summaryService = summaryService;
    }

    /**
     * Build comprehensive health context for a user.
     * 
     * Aggregates all health data including profile, weight trends, goals, and
     * progress. Suitable for AI analysis.
     * 
     * @param userId the user ID
     * @return comprehensive health context as LinkedHashMap (preserves order)
     * @throws IllegalStateException if required data is missing
     */
    public Map<String, Object> buildContext(UUID userId) {
        // Fetch all required data
        HealthProfileEntity profile = profileService.find(userId)
                .orElseThrow(() -> new IllegalStateException("Health profile required"));

        List<WeightEntryEntity> weights = weightRepo.findTop30ByUserIdOrderByMeasuredAtDesc(userId);
        if (weights.isEmpty()) {
            throw new IllegalStateException("Weight data required");
        }

        GoalEntity activeGoal = goalRepo.findFirstByUserIdAndActiveTrue(userId)
                .orElseThrow(() -> new IllegalStateException("Active goal required"));

        // Use LinkedHashMap to preserve field order for stable hashing
        Map<String, Object> context = new LinkedHashMap<>();

        // Demographics
        addDemographicsSection(context, profile);

        // Current metrics
        addCurrentMetricsSection(context, profile, weights);

        // Weight trends
        addWeightTrendsSection(context, weights);

        // Activity & compliance
        addActivitySection(context, profile);

        // Dietary info
        addDietarySection(context, profile);

        // Goal details
        addGoalSection(context, activeGoal);

        // Goal progress
        addGoalProgressSection(context, activeGoal.getId(), userId);

        // Wellness summary
        addWellnessSection(context, profile);

        return context;
    }

    /**
     * Build user prompt for AI based on context.
     * 
     * Formats context into readable text suitable for system prompt inclusion.
     * 
     * @param context the context map from buildContext()
     * @return formatted user prompt string
     */
    public String buildUserPrompt(Map<String, Object> context) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== HEALTH CONTEXT FOR WELLNESS COACHING ===\n\n");

        // Demographics
        Map<?, ?> demo = (Map<?, ?>) context.getOrDefault("demographics", new HashMap<>());
        if (!demo.isEmpty()) {
            sb.append("PERSONAL INFO:\n");
            demo.forEach((k, v) -> sb.append("  ").append(k).append(": ").append(v).append("\n"));
            sb.append("\n");
        }

        // Current metrics
        Map<?, ?> metrics = (Map<?, ?>) context.getOrDefault("current_metrics", new HashMap<>());
        if (!metrics.isEmpty()) {
            sb.append("CURRENT STATUS:\n");
            metrics.forEach((k, v) -> sb.append("  ").append(k).append(": ").append(v).append("\n"));
            sb.append("\n");
        }

        // Weight trends
        Map<?, ?> trends = (Map<?, ?>) context.getOrDefault("weight_trends", new HashMap<>());
        if (!trends.isEmpty()) {
            sb.append("WEIGHT TRENDS:\n");
            trends.forEach((k, v) -> sb.append("  ").append(k).append(": ").append(v).append("\n"));
            sb.append("\n");
        }

        // Activity
        Map<?, ?> activity = (Map<?, ?>) context.getOrDefault("activity", new HashMap<>());
        if (!activity.isEmpty()) {
            sb.append("ACTIVITY & COMPLIANCE:\n");
            activity.forEach((k, v) -> sb.append("  ").append(k).append(": ").append(v).append("\n"));
            sb.append("\n");
        }

        // Dietary
        Map<?, ?> dietary = (Map<?, ?>) context.getOrDefault("dietary", new HashMap<>());
        if (!dietary.isEmpty()) {
            sb.append("DIETARY PREFERENCES:\n");
            dietary.forEach((k, v) -> sb.append("  ").append(k).append(": ").append(v).append("\n"));
            sb.append("\n");
        }

        // Goals
        Map<?, ?> goals = (Map<?, ?>) context.getOrDefault("active_goal", new HashMap<>());
        if (!goals.isEmpty()) {
            sb.append("ACTIVE GOAL:\n");
            goals.forEach((k, v) -> sb.append("  ").append(k).append(": ").append(v).append("\n"));
            sb.append("\n");
        }

        // Progress
        Map<?, ?> progress = (Map<?, ?>) context.getOrDefault("goal_progress", new HashMap<>());
        if (!progress.isEmpty()) {
            sb.append("GOAL PROGRESS:\n");
            progress.forEach((k, v) -> sb.append("  ").append(k).append(": ").append(v).append("\n"));
            sb.append("\n");
        }

        // Wellness
        Map<?, ?> wellness = (Map<?, ?>) context.getOrDefault("wellness", new HashMap<>());
        if (!wellness.isEmpty()) {
            sb.append("WELLNESS SUMMARY:\n");
            wellness.forEach((k, v) -> sb.append("  ").append(k).append(": ").append(v).append("\n"));
        }

        sb.append("\n=== COACHING TASK ===\n");
        sb.append("Based on this comprehensive health context, provide:\n");
        sb.append("1. Exactly 3 actionable recommendations (movement, recovery, mindset)\n");
        sb.append("2. One reflective question for journaling\n");
        sb.append("3. A 2-3 sentence motivational summary\n");
        sb.append("Keep all text concise and supportive. Reference specific data points when relevant.");

        return sb.toString();
    }

    private void addDemographicsSection(Map<String, Object> context, HealthProfileEntity profile) {
        Map<String, Object> demo = new LinkedHashMap<>();
        if (profile.getBirthYear() != null) {
            demo.put("age_year_born", profile.getBirthYear());
        }
        if (profile.getGender() != null) {
            demo.put("gender", profile.getGender());
        }
        demo.put("height_cm", profile.getHeightCm());
        context.put("demographics", demo);
    }

    private void addCurrentMetricsSection(Map<String, Object> context, HealthProfileEntity profile,
            List<WeightEntryEntity> weights) {
        Map<String, Object> metrics = new LinkedHashMap<>();

        if (!weights.isEmpty()) {
            WeightEntryEntity latest = weights.get(0);
            metrics.put("current_weight_kg", latest.getWeightKg());
            metrics.put("measured_at", latest.getMeasuredAt());

            double bmi = summaryService.bmi(profile.getHeightCm(), latest.getWeightKg());
            metrics.put("current_bmi", Math.round(bmi * 10.0) / 10.0);
        }

        if (profile.getBmiClassification() != null) {
            metrics.put("bmi_classification", profile.getBmiClassification());
        }

        if (profile.getWellnessScore() != null) {
            metrics.put("wellness_score", profile.getWellnessScore());
        }

        context.put("current_metrics", metrics);
    }

    private void addWeightTrendsSection(Map<String, Object> context, List<WeightEntryEntity> weights) {
        Map<String, Object> trends = new LinkedHashMap<>();

        if (weights.size() >= 1) {
            Double delta7d = summaryService.weightDelta7d(weights);
            if (delta7d != null) {
                trends.put("weight_change_7_days_kg", Math.round(delta7d * 100.0) / 100.0);
            }
        }

        trends.put("total_entries", weights.size());
        context.put("weight_trends", trends);
    }

    private void addActivitySection(Map<String, Object> context, HealthProfileEntity profile) {
        Map<String, Object> activity = new LinkedHashMap<>();

        if (profile.getBaselineActivityLevel() != null) {
            activity.put("baseline_activity_level", profile.getBaselineActivityLevel());
        }

        // Fitness assessment data
        if (profile.getFitnessAssessment() != null && !profile.getFitnessAssessment().isEmpty()) {
            Map<String, Object> fitness = profile.getFitnessAssessment();
            if (fitness.get("weekly_exercise_days") != null) {
                activity.put("weekly_exercise_days", fitness.get("weekly_exercise_days"));
            }
            if (fitness.get("exercise_preference") != null) {
                activity.put("exercise_preference", fitness.get("exercise_preference"));
            }
            if (fitness.get("current_fitness_level") != null) {
                activity.put("current_fitness_level", fitness.get("current_fitness_level"));
            }
        }

        if (profile.getFitnessAssessmentCompleted() != null) {
            activity.put("fitness_assessment_completed", profile.getFitnessAssessmentCompleted());
        }

        context.put("activity", activity);
    }

    private void addDietarySection(Map<String, Object> context, HealthProfileEntity profile) {
        Map<String, Object> dietary = new LinkedHashMap<>();

        if (profile.getDietaryPreferences() != null && !profile.getDietaryPreferences().isEmpty()) {
            dietary.put("preferences", profile.getDietaryPreferences());
        }

        if (profile.getDietaryRestrictions() != null && !profile.getDietaryRestrictions().isEmpty()) {
            dietary.put("restrictions", profile.getDietaryRestrictions());
        }

        context.put("dietary", dietary);
    }

    private void addGoalSection(Map<String, Object> context, GoalEntity goal) {
        Map<String, Object> goalMap = new LinkedHashMap<>();

        if (goal.getGoalType() != null) {
            goalMap.put("goal_type", goal.getGoalType().name());
        }

        if (goal.getNotes() != null) {
            goalMap.put("notes", goal.getNotes());
        }

        if (goal.getTargetWeightKg() != null) {
            goalMap.put("target_weight_kg", goal.getTargetWeightKg());
        }

        if (goal.getTargetActivityDaysPerWeek() != null) {
            goalMap.put("target_activity_days_per_week", goal.getTargetActivityDaysPerWeek());
        }

        if (goal.getCreatedAt() != null) {
            goalMap.put("goal_started", goal.getCreatedAt());
        }

        context.put("active_goal", goalMap);
    }

    private void addGoalProgressSection(Map<String, Object> context, UUID goalId, UUID userId) {
        Map<String, Object> progress = new LinkedHashMap<>();

        Optional<GoalProgressEntity> latestOpt = progressRepo.findFirstByGoalIdOrderByRecordedAtDesc(goalId);

        if (latestOpt.isPresent()) {
            GoalProgressEntity latest = latestOpt.get();
            progress.put("progress_percentage", latest.getProgressPercentage());
            progress.put("current_value", latest.getCurrentValue());
            progress.put("is_on_track", latest.getIsOnTrack());
            progress.put("days_remaining", latest.getDaysRemaining());
            progress.put("milestones_completed", latest.getMilestonesCompleted());
            progress.put("last_recorded_at", latest.getRecordedAt());
        }

        List<GoalProgressEntity> history = progressRepo.findTop30ByGoalIdOrderByRecordedAtDesc(goalId);
        progress.put("total_progress_records", history.size());

        context.put("goal_progress", progress);
    }

    private void addWellnessSection(Map<String, Object> context, HealthProfileEntity profile) {
        Map<String, Object> wellness = new LinkedHashMap<>();

        if (profile.getWellnessScore() != null) {
            wellness.put("overall_wellness_score", profile.getWellnessScore());
        }

        // Add description based on score
        if (profile.getWellnessScore() != null) {
            int score = profile.getWellnessScore();
            String description;
            if (score >= 90) {
                description = "Excellent";
            } else if (score >= 75) {
                description = "Very Good";
            } else if (score >= 60) {
                description = "Good";
            } else if (score >= 45) {
                description = "Fair";
            } else {
                description = "Needs Improvement";
            }
            wellness.put("wellness_level", description);
        }

        wellness.put("last_updated", OffsetDateTime.now());
        context.put("wellness", wellness);
    }
}
