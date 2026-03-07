package com.erikallas.ndl.auth.api;

import com.erikallas.ndl.auth.api.dto.PasswordResetResponse;
import com.erikallas.ndl.auth.api.dto.CompleteResetRequest;
import com.erikallas.ndl.auth.api.dto.PasswordResetRequest;
import com.erikallas.ndl.auth.service.PasswordResetService;
import com.erikallas.ndl.auth.user.model.UserEntity;
import com.erikallas.ndl.auth.user.model.UserRepository;

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
    public ResponseEntity<PasswordResetResponse> requestReset(@RequestBody PasswordResetRequest request) {
        // Validate request
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        // Request reset (generates token, no error if email doesn't exist)
        passwordResetService.requestPasswordReset(request.getEmail());

        // Always return success for security
        return ResponseEntity.ok(new PasswordResetResponse("Password reset link sent to email (if account exists)"));
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
    public ResponseEntity<PasswordResetResponse> completeReset(@RequestBody CompleteResetRequest request) {
        // Validate request
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getToken() == null || request.getToken().isEmpty()) {
            throw new IllegalArgumentException("Token is required");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
            throw new IllegalArgumentException("New password is required");
        }
        // Find user by email
        UserEntity user = userRepository.findByEmailIgnoreCase(request.getEmail()).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        // Complete password reset
        boolean success = passwordResetService.completePasswordReset(request.getToken(), request.getNewPassword(),
                request.getEmail());
        if (!success) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        return ResponseEntity.ok(new PasswordResetResponse("Password reset successfully"));
    }
}
