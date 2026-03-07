package com.erikallas.ndl.auth.api;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erikallas.ndl.auth.user.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@SecurityRequirement(name = "bearerAuth")
@RestController
public class MeController {

    private static final Logger log = LoggerFactory.getLogger(MeController.class);
    private final UserService userService;

    public MeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/me")
    public Map<String, Object> me(JwtAuthenticationToken auth) {
        var jwt = auth.getToken();

        log.info("GET /api/me - issuer: {}, sub: {}, has email: {}", jwt.getIssuer(), jwt.getSubject(),
                jwt.getClaimAsString("email") != null);

        // Sync user from JWT (saves email from OAuth providers)
        try {
            userService.ensureUserFromJwt(auth);
            log.debug("User synced from JWT: sub={}", jwt.getSubject());
        } catch (Exception e) {
            log.error("Failed to sync user from JWT: {}", e.getMessage(), e);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sub", jwt.getSubject());
        out.put("email", jwt.getClaimAsString("email")); // may be null
        out.put("aud", jwt.getAudience()); // non-null list
        out.put("iss", jwt.getIssuer() != null ? jwt.getIssuer().toString() : null);
        out.put("scope", jwt.getClaimAsString("scope")); // may be null
        out.put("claims", jwt.getClaims().keySet()); // helpful for debugging
        return out;
    }
}
