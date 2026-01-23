package com.erikallas.ndl.health.profile;

import com.erikallas.ndl.user.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    public static class UpsertProfileRequest {
        public Integer birthYear;
        public String gender;

        @Min(value = 50, message = "heightCm must be >= 50")
        public int heightCm;

        @NotBlank(message = "baselineActivityLevel is required")
        // allowed values: sedentary, light, moderate, active, very_active
        @Pattern(regexp = "^(sedentary|light|moderate|active|very_active)$", message = "baselineActivityLevel must be one of: sedentary, light, moderate, active, very_active")
        public String baselineActivityLevel;
    }

    @GetMapping("/api/profile")
    public HealthProfileEntity getProfile(JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        return profileService.find(user.getId()).orElse(null);
    }

    @PostMapping("/api/profile")
    public HealthProfileEntity upsertProfile(@Valid @RequestBody UpsertProfileRequest body,
            JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        return profileService.upsert(user.getId(), body.birthYear, body.gender, body.heightCm,
                body.baselineActivityLevel);
    }
}
