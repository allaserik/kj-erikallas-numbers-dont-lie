package com.erikallas.ndl.export;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.erikallas.ndl.auth.twofactor.TwoFactorSecretEntity;
import com.erikallas.ndl.auth.twofactor.TwoFactorSecretRepository;
import com.erikallas.ndl.auth.user.model.UserEntity;
import com.erikallas.ndl.auth.user.model.UserRepository;
import com.erikallas.ndl.health.goal.GoalEntity;
import com.erikallas.ndl.health.goal.GoalProgressEntity;
import com.erikallas.ndl.health.goal.GoalProgressRepository;
import com.erikallas.ndl.health.goal.GoalRepository;
import com.erikallas.ndl.health.goal.GoalType;
import com.erikallas.ndl.health.profile.HealthProfileEntity;
import com.erikallas.ndl.health.profile.HealthProfileRepository;
import com.erikallas.ndl.health.weight.WeightEntryEntity;
import com.erikallas.ndl.health.weight.WeightEntryRepository;
import com.erikallas.ndl.privacy.PrivacyPreferencesEntity;
import com.erikallas.ndl.privacy.PrivacyPreferencesRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DataExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HealthProfileRepository healthProfileRepository;

    @Autowired
    private PrivacyPreferencesRepository privacyPreferencesRepository;

    @Autowired
    private TwoFactorSecretRepository twoFactorSecretRepository;

    @Autowired
    private WeightEntryRepository weightEntryRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private GoalProgressRepository goalProgressRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void exportIncludesHistoricalTimestamps() throws Exception {
        String auth0Sub = "test-export-" + UUID.randomUUID();
        String email = "export+" + UUID.randomUUID() + "@example.com";

        OffsetDateTime now = OffsetDateTime.now();
        UUID userId = UUID.randomUUID();

        userRepository.save(new UserEntity(userId, auth0Sub, email, now.minusDays(40)));

        var profile = new HealthProfileEntity(
                userId,
                1992,
                "male",
                182,
                "moderate",
                now.minusDays(39),
                now.minusDays(1));
        profile.setDietaryPreferences(List.of("high_protein"));
        profile.setDietaryRestrictions(List.of("lactose"));
        profile.setFitnessAssessment(Map.of("weekly_activity_days", 3));
        profile.setFitnessAssessmentCompleted(true);
        healthProfileRepository.save(profile);

        privacyPreferencesRepository.save(new PrivacyPreferencesEntity(
                userId,
                true,
                now.minusDays(35),
                true,
                false,
                true,
                now.minusDays(35),
                now.minusDays(2)));

        twoFactorSecretRepository.save(new TwoFactorSecretEntity(
                userId,
                "enc-secret",
                true,
                now.minusDays(10),
                now.minusDays(12),
                now.minusDays(2)));

        weightEntryRepository.save(new WeightEntryEntity(
                UUID.randomUUID(),
                userId,
                now.minusDays(5),
                84.2,
                "weekly checkin"));

        var goalId = UUID.randomUUID();
        goalRepository.save(new GoalEntity(
                goalId,
                userId,
                GoalType.WEIGHT_LOSS,
                78.0,
                4,
                "cutting phase",
                true,
                now.minusDays(20),
                now.minusDays(3)));

        var progress = new GoalProgressEntity(
                UUID.randomUUID(),
                goalId,
                userId,
                new BigDecimal("84.2"),
                40,
                true,
                60,
                now.minusDays(4),
                now.minusDays(4),
                now.minusDays(4));
        progress.setMilestonesCompleted(2);
        progress.setMilestoneDetails(List.of(Map.of("pct", 10), Map.of("pct", 20)));
        goalProgressRepository.save(progress);

        jdbcTemplate.update(
                "INSERT INTO ai_insights (id, user_id, goal_id, input_hash, model, payload, created_at) "
                        + "VALUES (?, ?, ?, ?, ?, CAST(? AS jsonb), ?)",
                UUID.randomUUID(), userId, goalId, "hash-1", "gpt-4o-mini", "{\"summary\":\"Nice progress\"}",
                now.minusDays(1));

        mockMvc.perform(get("/api/export")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(auth0Sub)
                                .claim("email", email)
                                .claim("iss", "numbers-dont-lie"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exported_at", not(emptyOrNullString())))
                .andExpect(jsonPath("$.account.created_at", not(emptyOrNullString())))
                .andExpect(jsonPath("$.health_profile.created_at", not(emptyOrNullString())))
                .andExpect(jsonPath("$.privacy_preferences.consent_given_at", not(emptyOrNullString())))
                .andExpect(jsonPath("$.weight_entries[0].measured_at", not(emptyOrNullString())))
                .andExpect(jsonPath("$.goals[0].created_at", not(emptyOrNullString())))
                .andExpect(jsonPath("$.goal_progress[0].recorded_at", not(emptyOrNullString())))
                .andExpect(jsonPath("$.ai_insights[0].created_at", not(emptyOrNullString())));
    }
}
