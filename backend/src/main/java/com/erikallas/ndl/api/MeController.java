package com.erikallas.ndl.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

    @GetMapping("/api/me")
    public Map<String, Object> me(JwtAuthenticationToken auth) {
        var jwt = auth.getToken();

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
