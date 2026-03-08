package com.erikallas.ndl.health.wellness;

import com.erikallas.ndl.health.profile.HealthProfileEntity;
import com.erikallas.ndl.health.profile.BMICalculator;
import com.erikallas.ndl.health.profile.HealthProfileRepository;
import com.erikallas.ndl.health.goal.GoalProgressEntity;
import com.erikallas.ndl.health.goal.GoalProgressRepository;
import com.erikallas.ndl.health.goal.GoalRepository;
import com.erikallas.ndl.health.weight.WeightEntryEntity;
import com.erikallas.ndl.health.weight.WeightEntryRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for calculating and updating wellness scores.
 * 
 * Wellness score is a composite metric (0-100) that aggregates: - BMI
 * classification (30%) - Activity level (30%) - Goal progress (20%) - Health
 * habits (20%)
 * 
 * The score is calculated when health metrics change and stored in the
 * health_profiles table for trend analysis.
 */
@Service
public class WellnessScoreService {

    private final HealthProfileRepository profileRepository;
    private final GoalRepository goalRepository;
    private final GoalProgressRepository goalProgressRepository;
    private final WeightEntryRepository weightEntryRepository;

    public WellnessScoreService(HealthProfileRepository profileRepository, GoalRepository goalRepository,
            GoalProgressRepository goalProgressRepository, WeightEntryRepository weightEntryRepository) {
        this.profileRepository = profileRepository;
        this.goalRepository = goalRepository;
        this.goalProgressRepository = goalProgressRepository;
        this.weightEntryRepository = weightEntryRepository;
    }

    /**
     * Calculate wellness score for a user and update their profile.
     * 
     * This method: 1. Retrieves the user's current health profile 2. Calculates
     * component scores from their health data 3. Computes overall weighted score 4.
     * Persists the score to database
     * 
     * @param userId the user whose wellness score should be calculated
     * @return the calculated wellness score (0-100), or null if profile not found
     */
    @Transactional
    public Integer calculateAndUpdateWellnessScore(UUID userId) {
        var profile = profileRepository.findById(userId).orElse(null);
        if (profile == null) {
            return null;
        }

        // Calculate component scores from current health data
        int bmiScore = calculateBmiComponentScore(profile);
        int activityScore = calculateActivityComponentScore(profile);
        int goalScore = calculateGoalProgressComponentScore(userId);
        int habitsScore = calculateHabitsComponentScore(profile, userId);

        // Calculate overall weighted score
        int overallScore = WellnessScoreCalculator.calculateOverallScore(bmiScore, activityScore, goalScore,
                habitsScore);

        // Update and persist the profile
        profile.setWellnessScore(overallScore);
        profileRepository.save(profile);

        return overallScore;
    }

    /**
     * Calculate BMI component score from health profile.
     * 
     * Returns 0 if BMI classification is not available.
     * 
     * @param profile the health profile
     * @return BMI score (0-100)
     */
    private int calculateBmiComponentScore(HealthProfileEntity profile) {
        String bmiClassification = profile.getBmiClassification();
        if (bmiClassification == null || bmiClassification.isBlank()) {
            return 0; // No BMI data yet
        }
        return WellnessScoreCalculator.calculateBmiScore(bmiClassification);
    }

    /**
     * Calculate activity level component score from health profile.
     * 
     * Extracts weekly activity frequency from fitness assessment data. Returns 0 if
     * fitness assessment is not complete.
     * 
     * @param profile the health profile
     * @return activity score (0-100)
     */
    private int calculateActivityComponentScore(HealthProfileEntity profile) {
        // Check if fitness assessment is complete
        if (profile.getFitnessAssessmentCompleted() == null || !profile.getFitnessAssessmentCompleted()) {
            return 0; // Fitness assessment not completed yet
        }

        var fitnessAssessment = profile.getFitnessAssessment();
        if (fitnessAssessment == null) {
            return 0; // No fitness assessment data
        }

        // Extract weekly activity frequency from fitness assessment
        // Expected format: {"current_activity_frequency": 5, ...}
        Object frequencyObj = fitnessAssessment.get("current_activity_frequency");
        if (frequencyObj == null) {
            return 0; // No activity frequency data
        }

        try {
            // Handle both Integer and Number types from JSON
            int frequency;
            if (frequencyObj instanceof Number num) {
                frequency = num.intValue();
            } else if (frequencyObj instanceof String str) {
                frequency = Integer.parseInt(str);
            } else {
                return 0;
            }

            return WellnessScoreCalculator.calculateActivityScore(frequency);
        } catch (Exception e) {
            return 0; // Error parsing activity frequency
        }
    }

    /**
     * Calculate goal progress component score from active goal progress.
     * 
     * Scoring:
     * - No active goal: neutral 50
     * - Active goal with progress records: latest progress percentage
     * - Active goal with no progress records yet: 0
     */
    private int calculateGoalProgressComponentScore(UUID userId) {
        var activeGoal = goalRepository.findFirstByUserIdAndActiveTrue(userId);
        if (activeGoal.isEmpty()) {
            return 50;
        }

        return goalProgressRepository.findFirstByGoalIdOrderByRecordedAtDesc(activeGoal.get().getId())
                .map(GoalProgressEntity::getProgressPercentage)
                .filter(Objects::nonNull)
                .map(value -> clampScore(value.intValue()))
                .orElse(0);
    }

    /**
     * Calculate habits component score from concrete behavior signals.
     * 
     * Uses available data:
     * - Weight check-in consistency over the last 7 days
     * - Self-reported activity frequency from fitness assessment
     * 
     * If neither signal is available, returns neutral 50.
     */
    private int calculateHabitsComponentScore(HealthProfileEntity profile, UUID userId) {
        List<Integer> signals = new ArrayList<>();

        // Signal 1: recent check-in consistency (distinct days with entries in last 7 days)
        var recentEntries = weightEntryRepository.findTop30ByUserIdOrderByMeasuredAtDesc(userId);
        if (!recentEntries.isEmpty()) {
            OffsetDateTime since = OffsetDateTime.now().minusDays(7);
            long daysWithWeightEntries = recentEntries.stream()
                    .filter(entry -> entry.getMeasuredAt() != null && !entry.getMeasuredAt().isBefore(since))
                    .map(entry -> entry.getMeasuredAt().toLocalDate())
                    .distinct()
                    .count();
            int checkinConsistencyScore = (int) Math.min(100, Math.round((daysWithWeightEntries / 7.0) * 100.0));
            signals.add(checkinConsistencyScore);
        }

        // Signal 2: self-reported activity frequency from fitness assessment
        Integer activityFrequency = extractWeeklyActivityFrequency(profile);
        if (activityFrequency != null) {
            signals.add(WellnessScoreCalculator.calculateActivityScore(activityFrequency));
        }

        if (signals.isEmpty()) {
            return 50;
        }

        double avg = signals.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).average().orElse(50.0);
        return clampScore((int) Math.round(avg));
    }

    private Integer extractWeeklyActivityFrequency(HealthProfileEntity profile) {
        if (profile.getFitnessAssessment() == null) {
            return null;
        }
        Object frequencyObj = profile.getFitnessAssessment().get("current_activity_frequency");
        if (frequencyObj == null) {
            return null;
        }
        try {
            if (frequencyObj instanceof Number num) {
                return num.intValue();
            }
            if (frequencyObj instanceof String str) {
                return Integer.parseInt(str);
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    private int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }

    /**
     * Build weekly wellness score points for evolution charting.
     * 
     * Uses historical weight data and goal progress snapshots to reconstruct weekly
     * wellness trend.
     */
    public List<WellnessHistoryPointResponse> getWeeklyWellnessHistory(UUID userId, int weeks) {
        var profileOpt = profileRepository.findById(userId);
        if (profileOpt.isEmpty()) {
            return List.of();
        }
        HealthProfileEntity profile = profileOpt.get();

        List<WeightEntryEntity> weights = weightEntryRepository.findByUserIdOrderByMeasuredAtDesc(userId);
        if (weights.isEmpty()) {
            return List.of();
        }
        List<GoalProgressEntity> progressHistory = goalProgressRepository.findByUserIdOrderByRecordedAtDesc(userId);

        List<WellnessHistoryPointResponse> points = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int safeWeeks = Math.max(4, Math.min(52, weeks));

        for (int i = safeWeeks - 1; i >= 0; i--) {
            LocalDate weekEnd = today.minusWeeks(i);
            LocalDate weekStart = weekEnd.minusDays(6);

            Double latestWeight = latestWeightUpTo(weights, weekEnd);
            if (latestWeight == null) {
                continue;
            }

            int bmiScore = calculateBmiScoreAtWeight(profile, latestWeight);
            int activityScore = calculateActivityComponentScore(profile);
            int goalScore = calculateGoalProgressScoreAt(progressHistory, weekEnd);
            int habitsScore = calculateHabitsScoreForWindow(profile, weights, weekStart, weekEnd);

            int overallScore = WellnessScoreCalculator.calculateOverallScore(bmiScore, activityScore, goalScore,
                    habitsScore);
            points.add(new WellnessHistoryPointResponse(weekStart, weekEnd, overallScore));
        }

        return points;
    }

    private Double latestWeightUpTo(List<WeightEntryEntity> weights, LocalDate cutoffDate) {
        return weights.stream()
                .filter(w -> w.getMeasuredAt() != null && !w.getMeasuredAt().toLocalDate().isAfter(cutoffDate))
                .max(Comparator.comparing(WeightEntryEntity::getMeasuredAt))
                .map(WeightEntryEntity::getWeightKg)
                .orElse(null);
    }

    private int calculateBmiScoreAtWeight(HealthProfileEntity profile, double weightKg) {
        if (profile.getHeightCm() <= 0) {
            return 0;
        }
        try {
            var bmi = BMICalculator.calculateBMI(weightKg, profile.getHeightCm());
            String classification = BMICalculator.classifyBMI(bmi);
            return WellnessScoreCalculator.calculateBmiScore(classification);
        } catch (Exception e) {
            return 0;
        }
    }

    private int calculateGoalProgressScoreAt(List<GoalProgressEntity> progressHistory, LocalDate weekEnd) {
        return progressHistory.stream()
                .filter(p -> p.getRecordedAt() != null && !p.getRecordedAt().toLocalDate().isAfter(weekEnd))
                .max(Comparator.comparing(GoalProgressEntity::getRecordedAt))
                .map(GoalProgressEntity::getProgressPercentage)
                .filter(Objects::nonNull)
                .map(this::clampScore)
                .orElse(50);
    }

    private int calculateHabitsScoreForWindow(HealthProfileEntity profile, List<WeightEntryEntity> weights,
            LocalDate weekStart, LocalDate weekEnd) {
        List<Integer> signals = new ArrayList<>();

        long daysWithWeightEntries = weights.stream()
                .filter(entry -> entry.getMeasuredAt() != null)
                .map(entry -> entry.getMeasuredAt().toLocalDate())
                .filter(date -> !date.isBefore(weekStart) && !date.isAfter(weekEnd))
                .distinct()
                .count();
        int checkinConsistencyScore = (int) Math.min(100, Math.round((daysWithWeightEntries / 7.0) * 100.0));
        signals.add(checkinConsistencyScore);

        Integer activityFrequency = extractWeeklyActivityFrequency(profile);
        if (activityFrequency != null) {
            signals.add(WellnessScoreCalculator.calculateActivityScore(activityFrequency));
        }

        double avg = signals.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).average().orElse(50.0);
        return clampScore((int) Math.round(avg));
    }

    /**
     * Get the current wellness score for a user without recalculating.
     * 
     * @param userId the user ID
     * @return the stored wellness score, or null if profile not found
     */
    public Integer getWellnessScore(UUID userId) {
        return profileRepository.findById(userId).map(HealthProfileEntity::getWellnessScore).orElse(null);
    }

    /**
     * Get a human-readable description of the wellness score.
     * 
     * @param userId the user ID
     * @return description like "Excellent", "Good", "Needs Improvement", etc.
     */
    public String getWellnessScoreDescription(UUID userId) {
        Integer score = getWellnessScore(userId);
        if (score == null) {
            return "No data available";
        }
        return WellnessScoreCalculator.getScoreDescription(score);
    }
}
