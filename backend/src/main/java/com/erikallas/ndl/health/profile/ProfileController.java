package com.erikallas.ndl.health.profile;

import com.erikallas.ndl.common.api.dto.HealthProfileResponse;
import com.erikallas.ndl.common.api.mapper.ResponseMapper;
import com.erikallas.ndl.common.api.validation.OwnershipValidator;
import com.erikallas.ndl.health.wellness.WellnessScoreService;
import com.erikallas.ndl.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearerAuth")
@RestController
public class ProfileController {

    private final UserService userService;
    private final HealthProfileService profileService;
    private final WellnessScoreService wellnessScoreService;
    private final HealthProfileRepository profileRepository;

    public ProfileController(UserService userService, HealthProfileService profileService,
            WellnessScoreService wellnessScoreService, HealthProfileRepository profileRepository) {
        this.userService = userService;
        this.profileService = profileService;
        this.wellnessScoreService = wellnessScoreService;
        this.profileRepository = profileRepository;
    }

    /**
     * Get user's health profile.
     */
    @GetMapping("/api/profile")
    public HealthProfileResponse getProfile(JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        var entity = profileService.find(user.getId()).orElse(null);
        return ResponseMapper.toHealthProfileResponse(entity);
    }

    /**
     * Create or update user's health profile. Accepts all health data:
     * demographics, activity, dietary, fitness assessment.
     * 
     * Automatically recalculates wellness score after profile update.
     */
    @PostMapping("/api/profile")
    public HealthProfileResponse upsertProfile(@Valid @RequestBody HealthProfileRequest request,
            JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        var profile = profileService.upsert(user.getId(), request.getBirthYear(), request.getGender(),
                request.getHeightCm(), request.getBaselineActivityLevel(), request.getDietaryPreferences(),
                request.getDietaryRestrictions(), request.getFitnessAssessment(),
                request.getFitnessAssessmentCompleted());

        // Auto-calculate wellness score after profile update
        wellnessScoreService.calculateAndUpdateWellnessScore(user.getId());

        return ResponseMapper.toHealthProfileResponse(profile);
    }

    /**
     * Delete user's health profile (soft delete).
     * 
     * @param auth JWT authentication token
     */
    @DeleteMapping("/api/profile")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        var entity = profileService.find(user.getId()).orElse(null);
        OwnershipValidator.validateResourceExists(entity != null, "Health profile");
        OwnershipValidator.validateOwnership(entity.getUserId(), user.getId(), "health profile");
        profileRepository.delete(entity);
    }
}
