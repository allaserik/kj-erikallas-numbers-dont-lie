package com.erikallas.ndl.ai.insight;

import com.erikallas.ndl.ai.openai.OpenAiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AiInsightService {
    private static final Logger log = LoggerFactory.getLogger(AiInsightService.class);

    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private static final String PROMPT_VERSION = "v2-context-builder";

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
            return new AiInsightResult(parseAndValidate(cached.getPayload()), "cache", cached.getCreatedAt());
        }

        // No cache hit: call OpenAI (or fallback)
        if (!openAi.hasKey()) {
            log.warn("OPENAI_API_KEY missing, falling back to last insight. userId={}", userId);
            return fallbackToLast(userId);
        }

        String systemPrompt = "You are a supportive wellness coach. You must respond ONLY with valid JSON matching the provided schema. "
                + "Do not include any extra keys. Keep recommendations concise, actionable, and safe.";

        String userPrompt = contextBuilder.buildUserPrompt(context);

        try {
            String json = openAi.generateInsightJson(systemPrompt, userPrompt);
            AiPayload payload = parseAndValidate(json);

            AiInsightEntity stored = new AiInsightEntity(UUID.randomUUID(), userId, goalId, inputHash,
                    openAi.model(), writeJson(payload), OffsetDateTime.now());
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
            return new AiInsightResult(parseAndValidate(last.getPayload()), "cached", last.getCreatedAt());
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

            return new AiPayload(java.util.List.of(recs.get(0).asText(), recs.get(1).asText(), recs.get(2).asText()),
                    rq.asText(), sum.asText());
        } catch (Exception e) {
            throw new IllegalArgumentException("AI output not valid JSON schema", e);
        }
    }
}
