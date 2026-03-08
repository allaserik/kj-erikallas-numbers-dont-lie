package com.erikallas.ndl.ai.insight;

import com.erikallas.ndl.ai.openai.OpenAiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AiInsightService {
    private static final Logger log = LoggerFactory.getLogger(AiInsightService.class);

    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private static final String PROMPT_VERSION = "v3-grounded-safe-novel";
    private static final Pattern PERCENT_PATTERN = Pattern.compile("\\b(\\d{1,3})\\s*%");
    private static final Pattern UNSAFE_PATTERN = Pattern.compile(
            "\\b(diagnose|diagnosis|medication|dose|dosage|prescribe|cure|starve|starvation|purge|laxative|self-harm|suicide)\\b",
            Pattern.CASE_INSENSITIVE);

    private final AiInsightRepository repo;
    private final OpenAiClient openAi;
    private final InsightContextBuilder contextBuilder;
    private final ObjectMapper om;

    public AiInsightService(AiInsightRepository repo, OpenAiClient openAi, InsightContextBuilder contextBuilder,
            ObjectMapper om) {
        this.repo = repo;
        this.openAi = openAi;
        this.contextBuilder = contextBuilder;
        this.om = om;
    }

    public record AiPayload(java.util.List<String> recommendations, String reflection_question, String summary) {
    }

    public record AiInsightResult(AiPayload payload, String source, OffsetDateTime createdAt) {
    }

    public AiInsightResult getCurrent(UUID userId) {
        Map<String, Object> context;
        try {
            context = contextBuilder.buildContext(userId);
        } catch (Exception e) {
            log.info("Smart insight unavailable due to missing context. Returning fallback. userId={}, reason={}",
                    userId, e.getMessage());
            return fallbackToLast(userId);
        }

        String contextJson = writeJson(context);
        String inputHash = sha256(PROMPT_VERSION + "|" + contextJson);
        UUID goalId = extractGoalIdFromContext(context);

        // Cache hit?
        var cached = repo.findFirstByUserIdAndInputHashOrderByCreatedAtDesc(userId, inputHash).orElse(null);
        if (cached != null && cached.getCreatedAt().isAfter(OffsetDateTime.now().minus(CACHE_TTL))) {
            return new AiInsightResult(parseAndValidateStored(writeJson(cached.getPayload())), "cache",
                    cached.getCreatedAt());
        }

        Set<String> recentRecommendations = getRecentRecommendations(userId, 5);
        String userPrompt = buildUserPromptWithGuards(context, recentRecommendations);

        // No cache hit: call OpenAI (or fallback)
        if (!openAi.hasKey()) {
            log.warn("OPENAI_API_KEY missing, falling back to last insight. userId={}", userId);
            return fallbackToLast(userId);
        }

        String systemPrompt = "You are a supportive wellness coach. You must respond ONLY with valid JSON matching the provided schema. "
                + "Do not include any extra keys. Ground every recommendation in provided user data only. "
                + "Do not invent metrics, percentages, timelines, or achievements. "
                + "Do not provide medical diagnosis, prescription, dosage, or extreme advice. "
                + "Keep recommendations concise, actionable, and safe.";

        try {
            String json = openAi.generateInsightJson(systemPrompt, userPrompt);
            AiPayload payload = parseAndValidateGenerated(json, context, recentRecommendations);

            AiInsightEntity stored = new AiInsightEntity(UUID.randomUUID(), userId, goalId, inputHash,
                    openAi.model(), om.valueToTree(payload), OffsetDateTime.now());
            repo.save(stored);

            return new AiInsightResult(payload, "openai", stored.getCreatedAt());
        } catch (Exception e) {
            log.warn("AI insight generation failed, falling back. userId={}, reason={}", userId, e.getMessage());
            return fallbackToLast(userId);
        }
    }

    private UUID extractGoalIdFromContext(Map<String, Object> context) {
        Object goalProgressObj = context.get("goal_progress");
        if (!(goalProgressObj instanceof Map<?, ?> goalProgress)) {
            return null;
        }
        Object goalIdObj = goalProgress.get("goal_id");
        if (goalIdObj instanceof String goalIdStr) {
            try {
                return UUID.fromString(goalIdStr);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private AiInsightResult fallbackToLast(UUID userId) {
        // seek to return last cached insight
        var last = repo.findFirstByUserIdOrderByCreatedAtDesc(userId).orElse(null);
        if (last != null) {
            return new AiInsightResult(parseAndValidateStored(writeJson(last.getPayload())), "cached",
                    last.getCreatedAt());
        }
        // structured default fallback (still valid schema)
        AiPayload payload = new AiPayload(
                java.util.List.of("AI is temporarily unavailable. Take a 10-minute walk or do light stretching.",
                        "Drink water and plan a short recovery break today.",
                        "Pick one small task and do 10 minutes of focused work with a timer."),
                "What is the smallest helpful thing you can do in the next 15 minutes?",
                "AI is unavailable right now, but you can still make progress today. Focus on one small step and protect your energy.");
        return new AiInsightResult(payload, "fallback", OffsetDateTime.now());
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

    private AiPayload parseAndValidateStored(String json) {
        return parseAndValidate(json, null, null, false);
    }

    private AiPayload parseAndValidateGenerated(String json, Map<String, Object> context, Set<String> recentNormalized) {
        return parseAndValidate(json, context, recentNormalized, true);
    }

    private AiPayload parseAndValidate(String json, Map<String, Object> context, Set<String> recentNormalized,
            boolean strictGuards) {
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

            AiPayload payload = new AiPayload(
                    java.util.List.of(recs.get(0).asText(), recs.get(1).asText(), recs.get(2).asText()),
                    rq.asText(), sum.asText());

            if (strictGuards) {
                validateGrounding(payload, context);
                validatePercentClaims(payload, context);
                validateSafety(payload);
                validateRecommendationNovelty(payload, recentNormalized);
            }

            return payload;
        } catch (Exception e) {
            throw new IllegalArgumentException("AI output not valid JSON schema", e);
        }
    }

    private void validateGrounding(AiPayload payload, Map<String, Object> context) {
        if (context == null) {
            return;
        }
        Set<String> dataTokens = extractKeyDataTokens(context);
        if (dataTokens.isEmpty()) {
            return;
        }

        boolean grounded = payload.recommendations().stream().anyMatch(r -> containsAnyToken(r, dataTokens))
                || containsAnyToken(payload.summary(), dataTokens);

        if (!grounded) {
            throw new IllegalArgumentException("insight missing concrete grounded reference");
        }
    }

    private void validatePercentClaims(AiPayload payload, Map<String, Object> context) {
        Set<Integer> allowedPercentages = extractAllowedPercentages(context);
        List<String> texts = new ArrayList<>(payload.recommendations());
        texts.add(payload.reflection_question());
        texts.add(payload.summary());

        for (String text : texts) {
            Matcher matcher = PERCENT_PATTERN.matcher(text);
            while (matcher.find()) {
                int claimed = Integer.parseInt(matcher.group(1));
                if (!allowedPercentages.contains(claimed)) {
                    throw new IllegalArgumentException("ungrounded percentage claim: " + claimed + "%");
                }
            }
        }
    }

    private void validateSafety(AiPayload payload) {
        List<String> texts = new ArrayList<>(payload.recommendations());
        texts.add(payload.reflection_question());
        texts.add(payload.summary());

        for (String text : texts) {
            if (UNSAFE_PATTERN.matcher(text).find()) {
                throw new IllegalArgumentException("unsafe medical/extreme wording detected");
            }
        }
    }

    private void validateRecommendationNovelty(AiPayload payload, Set<String> recentNormalized) {
        if (recentNormalized == null || recentNormalized.isEmpty()) {
            return;
        }
        for (String rec : payload.recommendations()) {
            if (recentNormalized.contains(normalizeText(rec))) {
                throw new IllegalArgumentException("repeated recommendation detected");
            }
        }
    }

    private Set<String> extractKeyDataTokens(Map<String, Object> context) {
        Set<String> out = new LinkedHashSet<>();
        addValueToken(out, nestedValue(context, "current_metrics", "current_weight_kg"));
        addValueToken(out, nestedValue(context, "current_metrics", "current_bmi"));
        addValueToken(out, nestedValue(context, "current_metrics", "wellness_score"));
        addValueToken(out, nestedValue(context, "goal_progress", "progress_percentage"));
        addValueToken(out, nestedValue(context, "goal_progress", "days_remaining"));
        addValueToken(out, nestedValue(context, "weight_trends", "weight_change_7_days_kg"));
        addValueToken(out, nestedValue(context, "active_goal", "target_weight_kg"));
        addValueToken(out, nestedValue(context, "active_goal", "target_activity_days_per_week"));
        return out;
    }

    private void addValueToken(Set<String> out, Object value) {
        if (value == null) {
            return;
        }
        String raw = String.valueOf(value).trim();
        if (raw.isBlank()) {
            return;
        }
        out.add(raw);

        String normalized = raw.replaceAll("\\.0+$", "");
        out.add(normalized);
        if (normalized.matches("-?\\d+")) {
            out.add(normalized + "%");
        }
    }

    @SuppressWarnings("unchecked")
    private Object nestedValue(Map<String, Object> context, String section, String key) {
        Object sectionObj = context.get(section);
        if (!(sectionObj instanceof Map<?, ?> sectionMap)) {
            return null;
        }
        return ((Map<String, Object>) sectionMap).get(key);
    }

    private Set<Integer> extractAllowedPercentages(Map<String, Object> context) {
        Set<Integer> allowed = new HashSet<>();
        if (context == null) {
            return allowed;
        }
        Object progress = nestedValue(context, "goal_progress", "progress_percentage");
        if (progress instanceof Number n) {
            allowed.add(n.intValue());
        } else if (progress instanceof String s) {
            try {
                allowed.add(Integer.parseInt(s.trim()));
            } catch (NumberFormatException ignored) {
                // ignore
            }
        }
        return allowed;
    }

    private boolean containsAnyToken(String text, Set<String> tokens) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String lower = text.toLowerCase();
        for (String token : tokens) {
            if (token != null && !token.isBlank() && lower.contains(token.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private Set<String> getRecentRecommendations(UUID userId, int count) {
        var page = repo.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, count));
        Set<String> out = new LinkedHashSet<>();
        for (AiInsightEntity insight : page.getContent()) {
            JsonNode recs = insight.getPayload() == null ? null : insight.getPayload().path("recommendations");
            if (recs == null || !recs.isArray()) {
                continue;
            }
            for (JsonNode rec : recs) {
                if (rec != null && rec.isTextual() && !rec.asText().isBlank()) {
                    out.add(normalizeText(rec.asText()));
                }
            }
        }
        return out;
    }

    private String buildUserPromptWithGuards(Map<String, Object> context, Set<String> recentRecommendations) {
        StringBuilder sb = new StringBuilder(contextBuilder.buildUserPrompt(context));
        sb.append("\n\n=== STRICT OUTPUT RULES ===\n");
        sb.append("- Mention at least one concrete metric from the provided context in recommendations or summary.\n");
        sb.append("- If you mention any percentage, it must exactly match a provided percentage from context.\n");
        sb.append("- Avoid diagnosis, prescriptions, dosages, and extreme advice.\n");
        if (recentRecommendations != null && !recentRecommendations.isEmpty()) {
            sb.append("- Do not repeat recommendations from recent insights; provide fresh wording and actions.\n");
        }
        return sb.toString();
    }

    private String normalizeText(String s) {
        return s.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
