package com.erikallas.ndl.health.wellness;

import com.erikallas.ndl.health.profile.HealthProfileEntity;
import com.erikallas.ndl.health.profile.HealthProfileRepository;
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

    public WellnessScoreService(HealthProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    /**
     * Calculate wellness score for a user and update their profile.
     * 
     * This method: 1. Retrieves the user's current health profile 2. Calculates
     * component scores from their health data 3. Computes overall weighted score 4.
     * Persists the score to database
     * 
     * Note: Goal progress and health habits scores default to 50 (neutral) until
     * goal tracking and habit compliance systems are implemented.
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
        int goalScore = 50; // Default neutral score - will be updated when goal tracking implemented
        int habitsScore = 50; // Default neutral score - will be updated when habit tracking implemented

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
