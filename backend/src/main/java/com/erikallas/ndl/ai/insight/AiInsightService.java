package com.erikallas.ndl.ai.insight;

import com.erikallas.ndl.ai.openai.OpenAiClient;
import com.erikallas.ndl.health.goal.GoalService;
import com.erikallas.ndl.health.summary.HealthSummaryService;
import com.erikallas.ndl.health.profile.HealthProfileService;
import com.erikallas.ndl.health.weight.WeightEntryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AiInsightService {

    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private final AiInsightRepository repo;
    private final OpenAiClient openAi;
    private final GoalService goalService;
    private final HealthProfileService profileService;
    private final WeightEntryRepository weightRepo;
    private final HealthSummaryService summaryService;
    private final ObjectMapper om;

    public AiInsightService(
            AiInsightRepository repo,
            OpenAiClient openAi,
            GoalService goalService,
            HealthProfileService profileService,
            WeightEntryRepository weightRepo,
            HealthSummaryService summaryService,
            ObjectMapper om) {
        this.repo = repo;
        this.openAi = openAi;
        this.goalService = goalService;
        this.profileService = profileService;
        this.weightRepo = weightRepo;
        this.summaryService = summaryService;
        this.om = om;
    }

    public record AiPayload(
            java.util.List<String> recommendations,
            String reflection_question,
            String summary) {
    }

    public record AiInsightResult(
            AiPayload payload,
            boolean cached,
            OffsetDateTime createdAt) {
    }

    public AiInsightResult getCurrent(UUID userId) {
        var goal = goalService.getActive(userId);
        if (goal == null) {
            throw new IllegalStateException("Active goal required");
        }

        var profile = profileService.find(userId)
                .orElseThrow(() -> new IllegalStateException("Profile required"));

        var weights = weightRepo.findTop30ByUserIdOrderByMeasuredAtDesc(userId);
        if (weights.isEmpty()) {
            throw new IllegalStateException("Weight data required");
        }

        var latest = weights.get(0);
        double bmi = summaryService.bmi(profile.getHeightCm(), latest.getWeightKg());
        Double delta7d = summaryService.weightDelta7d(weights);

        // Build a stable snapshot for hashing (LinkedHashMap preserves order)
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("goalType", goal.getGoalType().name());
        snapshot.put("targetWeightKg", goal.getTargetWeightKg());
        snapshot.put("targetActivityDaysPerWeek", goal.getTargetActivityDaysPerWeek());
        snapshot.put("heightCm", profile.getHeightCm());
        snapshot.put("latestWeightKg", latest.getWeightKg());
        snapshot.put("bmi", Math.round(bmi * 10.0) / 10.0);
        snapshot.put("weightDelta7d", delta7d);

        String snapshotJson = writeJson(snapshot);
        String inputHash = sha256(snapshotJson);

        // Cache hit?
        var cached = repo.findFirstByUserIdAndInputHashOrderByCreatedAtDesc(userId, inputHash).orElse(null);
        if (cached != null && cached.getCreatedAt().isAfter(OffsetDateTime.now().minus(CACHE_TTL))) {
            return new AiInsightResult(parseAndValidate(cached.getPayload()), true, cached.getCreatedAt());
        }

        // No cache hit: call OpenAI (or fallback)
        if (!openAi.hasKey()) {
            return fallbackToLast(userId, "OPENAI_API_KEY missing");
        }

        String systemPrompt = "You are a supportive wellness coach. You must respond ONLY with valid JSON matching the provided schema. "
                + "Do not include any extra keys. Keep recommendations concise, actionable, and safe.";

        String userPrompt = "User snapshot (JSON): " + snapshotJson + "\n\n"
                + "Task: Provide exactly 3 short recommendations (movement, recovery, focus), "
                + "1 reflective question (journaling style), and a 2-3 sentence motivational summary. "
                + "Reference the user's goal type and targets. Avoid medical claims.";

        try {
            String json = openAi.generateInsightJson(systemPrompt, userPrompt);
            AiPayload payload = parseAndValidate(json);

            AiInsightEntity stored = new AiInsightEntity(
                    UUID.randomUUID(),
                    userId,
                    goal.getId(),
                    inputHash,
                    openAi.model(),
                    writeJson(payload),
                    OffsetDateTime.now());
            repo.save(stored);

            return new AiInsightResult(payload, false, stored.getCreatedAt());
        } catch (Exception e) {
            return fallbackToLast(userId, e.getMessage());
        }
    }

    private AiInsightResult fallbackToLast(UUID userId, String reason) {
        var last = repo.findFirstByUserIdOrderByCreatedAtDesc(userId).orElse(null);
        if (last != null) {
            return new AiInsightResult(parseAndValidate(last.getPayload()), true, last.getCreatedAt());
        }
        // structured fallback (still valid schema)
        AiPayload payload = new AiPayload(
                java.util.List.of(
                        "AI is temporarily unavailable. Take a 10-minute walk or do light stretching.",
                        "Drink water and plan a short recovery break today.",
                        "Pick one small task and do 10 minutes of focused work with a timer."),
                "What is the smallest helpful thing you can do in the next 15 minutes?",
                "AI is unavailable right now, but you can still make progress today. Focus on one small step and protect your energy.");
        return new AiInsightResult(payload, true, OffsetDateTime.now());
    }

    private String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Hashing failed", e);
        }
    }

    private String writeJson(Object obj) {
        try {
            return om.writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalStateException("JSON serialization failed", e);
        }
    }

    private AiPayload parseAndValidate(String json) {
        try {
            JsonNode node = om.readTree(json);

            JsonNode recs = node.get("recommendations");
            JsonNode rq = node.get("reflection_question");
            JsonNode sum = node.get("summary");

            if (recs == null || !recs.isArray() || recs.size() != 3) {
                throw new IllegalArgumentException("recommendations must be array of size 3");
            }
            for (JsonNode r : recs) {
                if (!r.isTextual() || r.asText().isBlank() || r.asText().length() > 220) {
                    throw new IllegalArgumentException("invalid recommendation");
                }
            }
            if (rq == null || !rq.isTextual() || rq.asText().isBlank() || rq.asText().length() > 220) {
                throw new IllegalArgumentException("invalid reflection_question");
            }
            if (sum == null || !sum.isTextual() || sum.asText().isBlank() || sum.asText().length() > 400) {
                throw new IllegalArgumentException("invalid summary");
            }

            return new AiPayload(
                    java.util.List.of(recs.get(0).asText(), recs.get(1).asText(), recs.get(2).asText()),
                    rq.asText(),
                    sum.asText());
        } catch (Exception e) {
            throw new IllegalArgumentException("AI output not valid JSON schema", e);
        }
    }
}
