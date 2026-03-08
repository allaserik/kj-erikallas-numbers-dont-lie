package com.erikallas.ndl.privacy;

import com.erikallas.ndl.auth.user.UserService;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "bearerAuth")
@RestController
public class PrivacyPreferencesController {

    private final UserService userService;
    private final PrivacyPreferencesService privacyPreferencesService;

    public PrivacyPreferencesController(UserService userService, PrivacyPreferencesService privacyPreferencesService) {
        this.userService = userService;
        this.privacyPreferencesService = privacyPreferencesService;
    }

    public static class PrivacyPreferencesRequest {
        @NotNull
        @JsonProperty("data_usage_consent")
        public Boolean dataUsageConsent;

        @NotNull
        @JsonProperty("allow_anonymized_analytics")
        public Boolean allowAnonymizedAnalytics;

        @NotNull
        @JsonProperty("public_profile_visible")
        public Boolean publicProfileVisible;

        @NotNull
        @JsonProperty("email_notifications_enabled")
        public Boolean emailNotificationsEnabled;
    }

    public record PrivacyPreferencesResponse(
            @JsonProperty("data_usage_consent") boolean dataUsageConsent,
            @JsonProperty("consent_given_at") OffsetDateTime consentGivenAt,
            @JsonProperty("allow_anonymized_analytics") boolean allowAnonymizedAnalytics,
            @JsonProperty("public_profile_visible") boolean publicProfileVisible,
            @JsonProperty("email_notifications_enabled") boolean emailNotificationsEnabled,
            @JsonProperty("updated_at") OffsetDateTime updatedAt) {
    }

    @GetMapping("/api/privacy-preferences")
    public PrivacyPreferencesResponse getPreferences(JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        var prefs = privacyPreferencesService.getOrDefault(user.getId());
        return toResponse(prefs);
    }

    @PostMapping("/api/privacy-preferences")
    public PrivacyPreferencesResponse upsertPreferences(@Valid @RequestBody PrivacyPreferencesRequest request,
            JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        var prefs = privacyPreferencesService.upsert(user.getId(), request.dataUsageConsent,
                request.allowAnonymizedAnalytics, request.publicProfileVisible, request.emailNotificationsEnabled);
        return toResponse(prefs);
    }

    private PrivacyPreferencesResponse toResponse(PrivacyPreferencesEntity entity) {
        return new PrivacyPreferencesResponse(
                entity.isDataUsageConsent(),
                entity.getConsentGivenAt(),
                entity.isAllowAnonymizedAnalytics(),
                entity.isPublicProfileVisible(),
                entity.isEmailNotificationsEnabled(),
                entity.getUpdatedAt());
    }
}
