package com.erikallas.ndl.ai.insight;

import com.erikallas.ndl.ai.insight.AiInsightService.AiInsightResult;
import com.erikallas.ndl.user.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "bearerAuth")
@RestController
public class AiInsightController {

    private final UserService userService;
    private final AiInsightService insightService;

    public AiInsightController(UserService userService, AiInsightService insightService) {
        this.userService = userService;
        this.insightService = insightService;
    }

    @GetMapping("/api/insights/current")
    public AiInsightResult current(JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        return insightService.getCurrent(user.getId());
    }
}
