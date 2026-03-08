package com.erikallas.ndl.health.summary;

import com.erikallas.ndl.auth.user.UserService;
import com.erikallas.ndl.common.api.dto.ApiSuccess;
import com.erikallas.ndl.health.profile.HealthProfileEntity;
import com.erikallas.ndl.health.profile.HealthProfileService;
import com.erikallas.ndl.health.weight.WeightEntryEntity;
import com.erikallas.ndl.health.weight.WeightEntryRepository;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

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
    public ApiSuccess<HealthSummaryDto> summary(JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);

        HealthProfileEntity profile = profileService.find(user.getId())
                .orElseThrow(() -> new IllegalStateException("Profile required"));

        var weights = weightRepo.findTop30ByUserIdOrderByMeasuredAtDesc(user.getId());
        if (weights.isEmpty()) {
            throw new IllegalStateException("Weight data required");
        }

        var latest = weights.get(0);
        double bmi = summaryService.bmi(profile.getHeightCm(), latest.getWeightKg());
        Double delta7d = summaryService.weightDelta7d(weights);

        return ApiSuccess.of(new HealthSummaryDto(profile.getHeightCm(), latest.getWeightKg(),
                Math.round(bmi * 10.0) / 10.0, delta7d));
    }

    /**
     * Get weekly health summary for the past 7 days
     */
    @GetMapping("/api/summary/weekly")
    public ApiSuccess<PeriodSummaryDto> weeklySummary(JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        HealthProfileEntity profile = profileService.find(user.getId()).orElse(null);

        var weights = weightRepo.findTop90ByUserIdOrderByMeasuredAtDesc(user.getId());

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);

        Double weightChange = summaryService.weightDeltaForPeriod(weights, 7);
        List<WeightEntryEntity> periodWeights = summaryService.entriesInRange(weights, startDate, endDate);

        // Collect weight values for averaging (wellness score proxy)
        double avgWellness = periodWeights.isEmpty() ? (profile != null ? 75.0 : 50.0) : 75.0; // Placeholder: could
                                                                                               // include wellness
                                                                                               // calculation

        return ApiSuccess.of(new PeriodSummaryDto("weekly", startDate.toString(), endDate.toString(),
                !periodWeights.isEmpty() ? periodWeights.get(periodWeights.size() - 1).getWeightKg() : null,
                !periodWeights.isEmpty() ? periodWeights.get(0).getWeightKg() : null, weightChange, avgWellness,
                profile != null ? profile.getBaselineActivityLevel() : "UNKNOWN", 0, // Goal progress would need more
                                                                                     // context
                7, periodWeights.size()));
    }

    /**
     * Get monthly health summary for the past 30 days
     */
    @GetMapping("/api/summary/monthly")
    public ApiSuccess<PeriodSummaryDto> monthlySummary(JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        HealthProfileEntity profile = profileService.find(user.getId()).orElse(null);

        var weights = weightRepo.findTop90ByUserIdOrderByMeasuredAtDesc(user.getId());

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        Double weightChange = summaryService.weightDeltaForPeriod(weights, 30);
        List<WeightEntryEntity> periodWeights = summaryService.entriesInRange(weights, startDate, endDate);

        double avgWellness = periodWeights.isEmpty() ? (profile != null ? 75.0 : 50.0) : 75.0;

        return ApiSuccess.of(new PeriodSummaryDto("monthly", startDate.toString(), endDate.toString(),
                !periodWeights.isEmpty() ? periodWeights.get(periodWeights.size() - 1).getWeightKg() : null,
                !periodWeights.isEmpty() ? periodWeights.get(0).getWeightKg() : null, weightChange, avgWellness,
                profile != null ? profile.getBaselineActivityLevel() : "UNKNOWN", 0, 30, periodWeights.size()));
    }
}
