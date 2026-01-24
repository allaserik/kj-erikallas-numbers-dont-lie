package com.erikallas.ndl.health.goal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for GoalProgress data access.
 * 
 * Provides queries for tracking progress towards health goals, including
 * historical data for trend analysis and visualization.
 */
@Repository
public interface GoalProgressRepository extends JpaRepository<GoalProgressEntity, UUID> {

    /**
     * Get all progress records for a specific goal, ordered by most recent first.
     * 
     * Used to retrieve historical progress data for a goal.
     * 
     * @param goalId the goal ID
     * @return list of progress records ordered by recorded_at DESC
     */
    List<GoalProgressEntity> findByGoalIdOrderByRecordedAtDesc(UUID goalId);

    /**
     * Get all progress records for a user, ordered by most recent first.
     * 
     * Used to retrieve all progress data across user's active goals.
     * 
     * @param userId the user ID
     * @return list of progress records ordered by recorded_at DESC
     */
    List<GoalProgressEntity> findByUserIdOrderByRecordedAtDesc(UUID userId);

    /**
     * Get the most recent progress record for a goal.
     * 
     * Useful for getting current status without retrieving full history.
     * 
     * @param goalId the goal ID
     * @return optional containing most recent progress, or empty if none exists
     */
    Optional<GoalProgressEntity> findFirstByGoalIdOrderByRecordedAtDesc(UUID goalId);

    /**
     * Get progress records for a goal in descending time order, limited to N most
     * recent.
     * 
     * Used for pagination and limiting historical data returned to frontend.
     * 
     * @param goalId the goal ID
     * @param limit  maximum number of records to return
     * @return list of most recent progress records
     */
    List<GoalProgressEntity> findTop30ByGoalIdOrderByRecordedAtDesc(UUID goalId);

    /**
     * Check if any progress has been recorded for a goal.
     * 
     * @param goalId the goal ID
     * @return true if at least one progress record exists
     */
    boolean existsByGoalId(UUID goalId);
}
