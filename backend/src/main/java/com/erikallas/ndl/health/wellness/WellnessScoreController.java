package com.erikallas.ndl.health.wellness;

import com.erikallas.ndl.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API endpoints for wellness score management.
 * 
 * The wellness score is a composite metric (0-100) that aggregates: - BMI
 * classification (30%) - Activity level (30%) - Goal progress (20%) - Health
 * habits (20%)
 */
@SecurityRequirement(name = "bearerAuth")
@RestController
public class WellnessScoreController {

    private final UserService userService;
    private final WellnessScoreService wellnessScoreService;

    public WellnessScoreController(UserService userService, WellnessScoreService wellnessScoreService) {
        this.userService = userService;
        this.wellnessScoreService = wellnessScoreService;
    }

    /**
     * Get the current wellness score for the authenticated user.
     * 
     * Returns the stored wellness score without recalculating. Score is
     * automatically recalculated when health metrics change.
     * 
     * @param auth JWT authentication token with user context
     * @return WellnessScoreResponse with score (0-100) and description
     */
    @GetMapping("/api/wellness-score")
    public WellnessScoreResponse getWellnessScore(JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        Integer score = wellnessScoreService.getWellnessScore(user.getId());

        // Handle case where user has no profile yet
        if (score == null) {
            score = 0;
        }

        String description = wellnessScoreService.getWellnessScoreDescription(user.getId());
        return new WellnessScoreResponse(score, description);
    }

    /**
     * Calculate or recalculate the wellness score for the authenticated user.
     * 
     * This endpoint forces a recalculation based on the user's current health
     * profile data. Normally, the score is updated automatically when health
     * metrics change, but this endpoint allows manual recalculation.
     * 
     * @param auth JWT authentication token with user context
     * @return WellnessScoreResponse with newly calculated score (0-100)
     */
    @PostMapping("/api/wellness-score/calculate")
    public WellnessScoreResponse calculateWellnessScore(JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        Integer score = wellnessScoreService.calculateAndUpdateWellnessScore(user.getId());

        // Handle case where user has no profile yet
        if (score == null) {
            score = 0;
        }

        String description = wellnessScoreService.getWellnessScoreDescription(user.getId());
        return new WellnessScoreResponse(score, description);
    }
}
