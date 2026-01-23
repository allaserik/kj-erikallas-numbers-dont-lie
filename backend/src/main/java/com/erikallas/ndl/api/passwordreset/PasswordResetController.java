package com.erikallas.ndl.api.passwordreset;

import com.erikallas.ndl.auth.service.PasswordResetService;
import com.erikallas.ndl.user.model.UserEntity;
import com.erikallas.ndl.user.model.UserRepository;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/password-reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final UserRepository userRepository;

    public PasswordResetController(PasswordResetService passwordResetService, UserRepository userRepository) {
        this.passwordResetService = passwordResetService;
        this.userRepository = userRepository;
    }

    /**
     * Request a password reset. Sends a reset link to user's email with a token.
     * 
     * Request: { "email": "user@example.com" } Response: - 200: { "message":
     * "Password reset link sent to email" } Note: Always returns 200 for security
     * (don't reveal if email exists)
     */
    @PostMapping("/request")
    public ResponseEntity<?> requestReset(@RequestBody PasswordResetRequest request) {
        // Validate request
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        // Request reset (generates token, no error if email doesn't exist)
        passwordResetService.requestPasswordReset(request.getEmail());

        // Always return success for security
        return ResponseEntity.ok(Map.of("message", "Password reset link sent to email (if account exists)"));
    }

    /**
     * Complete password reset with token and new password.
     * 
     * Request: { "email": "user@example.com", "token": "uuid-string",
     * "newPassword": "NewPassword123!" } Response: - 200: { "message": "Password
     * reset successfully" } - 400: { "message": "Invalid or expired token" } or
     * validation error - 404: { "message": "User not found" }
     */
    @PostMapping("/complete")
    public ResponseEntity<?> completeReset(@RequestBody CompleteResetRequest request) {
        // Validate request
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        if (request.getToken() == null || request.getToken().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token is required"));
        }

        if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "New password is required"));
        }

        // Find user by email
        UserEntity user = userRepository.findByEmailIgnoreCase(request.getEmail()).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }

        try {
            // Complete password reset
            boolean success = passwordResetService.completePasswordReset(request.getToken(), request.getNewPassword(),
                    request.getEmail());

            if (!success) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired token"));
            }

            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));

        } catch (IllegalArgumentException e) {
            // Password validation error
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
