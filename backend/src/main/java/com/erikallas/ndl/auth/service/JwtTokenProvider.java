package com.erikallas.ndl.auth.service;

import com.erikallas.ndl.user.model.UserEntity;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Service for generating JWT tokens for email/password authentication. Uses
 * Spring Security's JwtEncoder for token generation.
 */
@Service
public class JwtTokenProvider {

    private final JwtEncoder jwtEncoder;

    @Value("${app.jwt.access-token-expiry-minutes:15}")
    private long accessTokenExpiryMinutes;

    @Value("${app.jwt.issuer:ndl-api}")
    private String issuer;

    public JwtTokenProvider(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    /**
     * Generate an access token for a user. Token contains: sub (user ID), email,
     * iss, exp, iat
     */
    public String generateAccessToken(UserEntity user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(accessTokenExpiryMinutes, ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(issuer).subject(user.getId().toString())
                .claim("email", user.getEmail()).issuedAt(now).expiresAt(expiresAt).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
