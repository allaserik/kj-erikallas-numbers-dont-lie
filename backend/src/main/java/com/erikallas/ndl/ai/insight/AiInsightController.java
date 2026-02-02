package com.erikallas.ndl.ai.insight;

import com.erikallas.ndl.ai.insight.AiInsightService.AiInsightResult;
import com.erikallas.ndl.common.api.dto.InsightResponse;
import com.erikallas.ndl.common.api.validation.OwnershipValidator;
import com.erikallas.ndl.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearerAuth")
@RestController
public class AiInsightController {

    private final UserService userService;
    private final AiInsightService insightService;
    private final AiInsightRepository insightRepository;

    public AiInsightController(UserService userService, AiInsightService insightService,
            AiInsightRepository insightRepository) {
        this.userService = userService;
        this.insightService = insightService;
        this.insightRepository = insightRepository;
    }

    /**
     * Get the current AI insight for the authenticated user.
     * 
     * @param auth JWT authentication token
     * @return the current insight
     */
    @GetMapping("/api/insights/current")
    public AiInsightResult current(JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        return insightService.getCurrent(user.getId());
    }

    /**
     * Delete an AI insight (soft delete).
     * 
     * @param id   the insight ID
     * @param auth JWT authentication token
     */
    @DeleteMapping("/api/insights/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        var entity = insightRepository.findByIdAndUserId(id, user.getId()).orElse(null);
        OwnershipValidator.validateResourceExists(entity != null, "Insight");
        insightRepository.delete(entity);
    }
}
