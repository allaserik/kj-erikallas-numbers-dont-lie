package com.erikallas.ndl.api.auth;

import com.erikallas.ndl.auth.service.AuthService;
import com.erikallas.ndl.auth.service.EmailService;
import com.erikallas.ndl.user.model.UserEntity;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    public AuthController(AuthService authService, EmailService emailService) {
        this.authService = authService;
        this.emailService = emailService;
    }

    /**
     * Register a new user with email and password.
     * 
     * Request: { "email": "user@example.com", "password": "Password123!" }
     * Response: - 201: { "id": "uuid", "email": "user@example.com",
     * "emailVerified": false, "message": "User registered. Check email for
     * verification code." } - 400: { "message": "Email already registered" } or
     * validation error
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRegisterRequest request) {
        // Validate request
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password is required"));
        }

        try {
            // Register user
            UserEntity user = authService.registerUser(request.getEmail(), request.getPassword());

            // Generate and send verification code
            emailService.generateVerificationCode(user);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("id", user.getId(), "email", user.getEmail(), "emailVerified", user.getEmailVerified(),
                            "message", "User registered. Check email for verification code."));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Authenticate user with email and password.
     * 
     * Request: { "email": "user@example.com", "password": "Password123!" }
     * Response: - 200: { "id": "uuid", "email": "user@example.com",
     * "emailVerified": true } - 401: { "message": "Invalid email or password" }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthLoginRequest request) {
        // Validate request
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password is required"));
        }

        // Authenticate user
        UserEntity user = authService.authenticateUser(request.getEmail(), request.getPassword());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password"));
        }

        // TODO: Generate JWT token and return it
        // For now, return user info
        return ResponseEntity.ok(Map.of("id", user.getId(), "email", user.getEmail(), "emailVerified",
                user.getEmailVerified(), "message", "Login successful. JWT token will be returned here."));
    }
}
