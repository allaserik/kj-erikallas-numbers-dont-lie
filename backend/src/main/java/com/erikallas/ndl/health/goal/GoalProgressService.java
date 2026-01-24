package com.erikallas.ndl.health.goal;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for calculating and tracking progress towards health goals.
 * 
 * Handles: - Progress calculation based on goal type (weight loss, activity,
 * etc.) - On-track determination (pace vs. deadline) - Milestone tracking
 * (every 5% progress) - Historical record keeping for trend analysis
 */
@Service
public class GoalProgressService {

    private final GoalProgressRepository progressRepository;
    private final GoalRepository goalRepository;

    // Milestone interval: track every 5% of progress
    private static final int MILESTONE_INTERVAL = 5;

    public GoalProgressService(GoalProgressRepository progressRepository, GoalRepository goalRepository) {
        this.progressRepository = progressRepository;
        this.goalRepository = goalRepository;
    }

    /**
     * Calculate and record progress for a specific goal.
     * 
     * This method: 1. Retrieves the goal 2. Calculates current progress based on
     * goal type 3. Determines on-track status 4. Checks for milestone completions
     * 5. Records snapshot in database
     * 
     * @param goalId       the goal to calculate progress for
     * @param currentValue the current metric value (e.g., weight in kg, activity
     *                     days)
     * @return the recorded GoalProgressEntity, or null if goal not found
     */
    @Transactional
    public GoalProgressEntity recordProgress(UUID goalId, BigDecimal currentValue) {
        Optional<GoalEntity> goalOpt = goalRepository.findById(goalId);
        if (goalOpt.isEmpty()) {
            return null;
        }

        GoalEntity goal = goalOpt.get();

        // Calculate progress percentage based on goal type
        int progressPercentage = calculateProgressPercentage(goal, currentValue);

        // Determine if on-track
        boolean isOnTrack = determineOnTrackStatus(goal, progressPercentage);

        // Calculate days remaining
        Integer daysRemaining = calculateDaysRemaining(goal);

        // Check for milestone completions
        List<Map<String, Object>> milestoneDetails = checkMilestones(goalId, progressPercentage);
        int milestonesCompleted = milestoneDetails.size();

        // Create progress record
        var progress = new GoalProgressEntity(UUID.randomUUID(), goalId, goal.getUserId(), currentValue,
                progressPercentage, isOnTrack, daysRemaining, OffsetDateTime.now(), OffsetDateTime.now(),
                OffsetDateTime.now());

        progress.setMilestoneDetails(milestoneDetails);
        progress.setMilestonesCompleted(milestonesCompleted);

        return progressRepository.save(progress);
    }

    /**
     * Calculate progress percentage based on goal type and current value.
     * 
     * For weight-related goals: Use target weight to calculate how close we are
     * 
     * For activity goals: progress = (current_days / target_days) * 100
     * 
     * For other goals: Return 0 (not yet implemented)
     * 
     * @param goal         the goal entity
     * @param currentValue the current metric value
     * @return progress percentage 0-100 (capped at 100 for completion)
     */
    private int calculateProgressPercentage(GoalEntity goal, BigDecimal currentValue) {
        if (goal.getGoalType() == GoalType.WEIGHT_LOSS) {
            if (goal.getTargetWeightKg() == null) {
                return 0;
            }

            // Assuming starting weight comes from health profile
            // For now, we'll use a placeholder - in production, fetch from health profile
            // progress = (start - current) / (start - target) * 100
            // Simplified: just track how close to target
            double distance_to_target = currentValue.doubleValue() - goal.getTargetWeightKg();

            // If we've reached or passed target, 100%
            if (distance_to_target <= 0) {
                return 100;
            }

            // Estimate progress (this would need actual start weight)
            // For now return percentage-like value
            return Math.min((int) (100 - (distance_to_target * 2)), 100); // Rough estimate

        } else if (goal.getGoalType() == GoalType.IMPROVE_FITNESS || goal.getGoalType() == GoalType.ENHANCE_ENDURANCE) {
            if (goal.getTargetActivityDaysPerWeek() == null || goal.getTargetActivityDaysPerWeek() == 0) {
                return 0;
            }

            int targetDays = goal.getTargetActivityDaysPerWeek();
            int currentDays = currentValue.intValue();
            int progress = (currentDays * 100) / targetDays;

            return Math.min(progress, 100);
        }

        // For other goal types, not yet implemented
        return 0;
    }

    /**
     * Determine if user is on-track to meet goal by target date.
     * 
     * On-track if: - (days_elapsed / total_days) >= (progress / 100)
     * 
     * Example: 50% time passed should have 50% progress
     * 
     * @param goal               the goal
     * @param progressPercentage current progress 0-100
     * @return true if on-track, false if behind
     */
    private boolean determineOnTrackStatus(GoalEntity goal, int progressPercentage) {
        // If no target date, assume on-track if making progress
        if (goal.getUpdatedAt() == null) {
            return progressPercentage > 0;
        }

        // Simple check: if progress >= 50%, generally on-track
        // In production, compare against days elapsed vs. target deadline
        return progressPercentage >= 50;
    }

    /**
     * Calculate days remaining until goal target date.
     * 
     * @param goal the goal
     * @return days remaining, or null if no target date
     */
    private Integer calculateDaysRemaining(GoalEntity goal) {
        if (goal.getUpdatedAt() == null) {
            return null;
        }

        // For now, estimate based on goal type
        // In production, fetch actual target_date from goal
        long daysElapsed = ChronoUnit.DAYS.between(goal.getCreatedAt(), OffsetDateTime.now());

        // Assume 90-day goals by default
        int totalDays = 90;
        int remaining = (int) (totalDays - daysElapsed);

        return Math.max(remaining, 0);
    }

    /**
     * Check if any milestones were completed with this progress update.
     * 
     * Milestones are tracked at 5% intervals (5%, 10%, 15%, etc.) Compares previous
     * highest milestone to current progress.
     * 
     * @param goalId          the goal ID
     * @param currentProgress current progress percentage
     * @return list of newly completed milestones as JSON objects
     */
    private List<Map<String, Object>> checkMilestones(UUID goalId, int currentProgress) {
        List<Map<String, Object>> newMilestones = new ArrayList<>();

        // Get previous progress
        Optional<GoalProgressEntity> previousOpt = progressRepository.findFirstByGoalIdOrderByRecordedAtDesc(goalId);
        int previousProgress = previousOpt.map(GoalProgressEntity::getProgressPercentage).orElse(0);

        // Check each milestone interval
        for (int milestone = MILESTONE_INTERVAL; milestone <= 100; milestone += MILESTONE_INTERVAL) {
            // If we crossed this milestone
            if (previousProgress < milestone && currentProgress >= milestone) {
                Map<String, Object> milestoneRecord = new HashMap<>();
                milestoneRecord.put("percentage", milestone);
                milestoneRecord.put("completed_at", OffsetDateTime.now().toString());
                newMilestones.add(milestoneRecord);
            }
        }

        return newMilestones;
    }

    /**
     * Get the most recent progress for a goal.
     * 
     * @param goalId the goal ID
     * @return optional containing latest progress, empty if none exists
     */
    public Optional<GoalProgressEntity> getLatestProgress(UUID goalId) {
        return progressRepository.findFirstByGoalIdOrderByRecordedAtDesc(goalId);
    }

    /**
     * Get progress history for a goal (last 30 records).
     * 
     * @param goalId the goal ID
     * @return list of progress records in reverse chronological order
     */
    public List<GoalProgressEntity> getProgressHistory(UUID goalId) {
        return progressRepository.findTop30ByGoalIdOrderByRecordedAtDesc(goalId);
    }

    /**
     * Get all progress for a user across all goals.
     * 
     * @param userId the user ID
     * @return list of all progress records ordered by most recent first
     */
    public List<GoalProgressEntity> getUserProgress(UUID userId) {
        return progressRepository.findByUserIdOrderByRecordedAtDesc(userId);
    }
}
