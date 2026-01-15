package com.erikallas.ndl.ai.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenAiClient {

    private final RestClient rest;
    @SuppressWarnings("unused")
    private final ObjectMapper om;
    private final Environment env;

    public OpenAiClient(ObjectMapper om, Environment env) {
        this.om = om;
        this.env = env;
        this.rest = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .build();
    }

    public String model() {
        return env.getProperty("OPENAI_MODEL", "gpt-4o-mini");
    }

    public boolean hasKey() {
        String key = env.getProperty("OPENAI_API_KEY");
        return key != null && !key.isBlank();
    }

    public String generateInsightJson(String systemPrompt, String userPrompt) {
        String key = env.getProperty("OPENAI_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY is not set");
        }

        // JSON Schema for strict structured output
        Map<String, Object> schema = Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", Map.of(
                        "recommendations", Map.of(
                                "type", "array",
                                "minItems", 3,
                                "maxItems", 3,
                                "items", Map.of("type", "string", "maxLength", 220)),
                        "reflection_question", Map.of("type", "string", "maxLength", 220),
                        "summary", Map.of("type", "string", "maxLength", 400)),
                "required", java.util.List.of("recommendations", "reflection_question", "summary"));

        Map<String, Object> body = Map.of(
                "model", model(),
                "input", java.util.List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)),
                "text", Map.of(
                        "format", Map.of(
                                "type", "json_schema",
                                "name", "wellness_insight_v1",
                                "strict", true,
                                "schema", schema)),
                "store", false);

        JsonNode resp = rest.post()
                .uri("/responses")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + key)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        // Extract first output_text.text
        JsonNode output = resp.path("output");
        for (JsonNode item : output) {
            JsonNode content = item.path("content");
            for (JsonNode c : content) {
                if ("output_text".equals(c.path("type").asText())) {
                    return c.path("text").asText();
                }
            }
        }
        throw new IllegalStateException("OpenAI response missing output_text");
    }
}
