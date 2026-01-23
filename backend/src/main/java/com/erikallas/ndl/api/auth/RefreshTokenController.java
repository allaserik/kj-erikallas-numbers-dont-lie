package com.erikallas.ndl.api.auth;

import com.erikallas.ndl.auth.model.RefreshTokenEntity;
import com.erikallas.ndl.auth.service.AuthService;
import com.erikallas.ndl.auth.service.RefreshTokenService;
import com.erikallas.ndl.user.model.UserEntity;
import com.erikallas.ndl.user.model.UserRepository;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Refresh token endpoint for obtaining new access tokens.
 * 
 * Users can use a valid refresh token to get a new access token without
 * re-entering credentials. Refresh tokens are valid for 7 days.
 */
@RestController
@RequestMapping("/api/auth")
public class RefreshTokenController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public RefreshTokenController(AuthService authService, RefreshTokenService refreshTokenService,
            UserRepository userRepository) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }

    /**
     * Refresh access token using a valid refresh token.
     * 
     * Request: { "refresh_token": "token-uuid" }
     * Response:
     * - 200: { "access_token": "jwt", "refresh_token": "uuid", "token_type": "Bearer", "expires_in": 900 }
     * - 400: { "message": "Invalid or expired refresh token" }
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        // Validate request
        if (request.getRefreshToken() == null || request.getRefreshToken().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Refresh token is required"));
        }

        // Validate refresh token
        RefreshTokenEntity tokenEntity = refreshTokenService.validateToken(request.getRefreshToken());

        if (tokenEntity == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid or expired refresh token"));
        }

        // Get user
        UserEntity user = userRepository.findById(tokenEntity.getUserId()).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "User not found"));
        }

        // Generate new access token (JWT would be created in real implementation)
        // For now, return a placeholder token that would be issued by JWT provider
        String newAccessToken = "Bearer " + java.util.UUID.randomUUID().toString();

        // Optionally: Generate new refresh token (rotating refresh tokens pattern)
        // String newRefreshToken = authService.generateRefreshToken(user);

        AuthTokenResponse response = new AuthTokenResponse(newAccessToken, request.getRefreshToken());

        return ResponseEntity.ok(response);
    }
}
