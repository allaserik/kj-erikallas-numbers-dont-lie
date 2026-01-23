package com.erikallas.ndl.health.summary;

import com.erikallas.ndl.health.profile.HealthProfileEntity;
import com.erikallas.ndl.health.profile.HealthProfileService;
import com.erikallas.ndl.health.weight.WeightEntryRepository;
import com.erikallas.ndl.user.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "bearerAuth")
@RestController
public class HealthSummaryController {

    private final UserService userService;
    private final HealthProfileService profileService;
    private final WeightEntryRepository weightRepo;
    private final HealthSummaryService summaryService;

    public HealthSummaryController(UserService userService, HealthProfileService profileService,
            WeightEntryRepository weightRepo, HealthSummaryService summaryService) {
        this.userService = userService;
        this.profileService = profileService;
        this.weightRepo = weightRepo;
        this.summaryService = summaryService;
    }

    @GetMapping("/api/summary")
    public HealthSummaryDto summary(JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);

        HealthProfileEntity profile = profileService.find(user.getId())
                .orElseThrow(() -> new IllegalStateException("Profile required"));

        var weights = weightRepo.findTop30ByUserIdOrderByMeasuredAtDesc(user.getId());
        if (weights.isEmpty()) {
            throw new IllegalStateException("Weight data required");
        }

        var latest = weights.get(0);
        double bmi = summaryService.bmi(profile.getHeightCm(), latest.getWeightKg());
        Double delta7d = summaryService.weightDelta7d(weights);

        return new HealthSummaryDto(profile.getHeightCm(), latest.getWeightKg(), Math.round(bmi * 10.0) / 10.0,
                delta7d);
    }
}
