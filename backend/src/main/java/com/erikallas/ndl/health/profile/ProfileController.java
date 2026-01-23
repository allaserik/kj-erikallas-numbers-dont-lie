package com.erikallas.ndl.health.profile;

import com.erikallas.ndl.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearerAuth")
@RestController
public class ProfileController {

    private final UserService userService;
    private final HealthProfileService profileService;

    public ProfileController(UserService userService, HealthProfileService profileService) {
        this.userService = userService;
        this.profileService = profileService;
    }

    /**
     * Get user's health profile.
     */
    @GetMapping("/api/profile")
    public HealthProfileEntity getProfile(JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        return profileService.find(user.getId()).orElse(null);
    }

    /**
     * Create or update user's health profile. Accepts all health data:
     * demographics, activity, dietary, fitness assessment.
     */
    @PostMapping("/api/profile")
    public HealthProfileEntity upsertProfile(@Valid @RequestBody HealthProfileRequest request,
            JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        return profileService.upsert(user.getId(), request.getBirthYear(), request.getGender(), request.getHeightCm(),
                request.getBaselineActivityLevel(), request.getDietaryPreferences(), request.getDietaryRestrictions(),
                request.getFitnessAssessment(), request.getFitnessAssessmentCompleted());
    }
}
