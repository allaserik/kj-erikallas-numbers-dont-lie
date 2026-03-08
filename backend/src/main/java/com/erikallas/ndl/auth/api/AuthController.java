package com.erikallas.ndl.auth.api;

import com.erikallas.ndl.auth.service.AuthService;
import com.erikallas.ndl.auth.user.model.UserEntity;
import com.erikallas.ndl.auth.api.dto.LoginResponse;
import com.erikallas.ndl.auth.api.dto.RegisterResponse;
import com.erikallas.ndl.auth.email.EmailService;
import com.erikallas.ndl.auth.api.dto.AuthLoginRequest;
import com.erikallas.ndl.auth.api.dto.AuthRegisterRequest;
import com.erikallas.ndl.auth.model.RefreshTokenEntity;
import com.erikallas.ndl.auth.twofactor.TwoFactorService;
import com.erikallas.ndl.common.api.dto.ApiSuccess;

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
    private final TwoFactorService twoFactorService;

    public AuthController(AuthService authService, EmailService emailService, TwoFactorService twoFactorService) {
        this.authService = authService;
        this.emailService = emailService;
        this.twoFactorService = twoFactorService;
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
    public ResponseEntity<ApiSuccess<RegisterResponse>> register(@RequestBody AuthRegisterRequest request) {
        // Validate request
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        // Register user
        UserEntity user = authService.registerUser(request.getEmail(), request.getPassword());
        // Generate and send verification code
        emailService.generateVerificationCode(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccess.of(new RegisterResponse(user.getId(), user.getEmail())));
    }

    /**
     * Authenticate user with email and password.
     * 
     * Request: { "email": "user@example.com", "password": "Password123!" }
     * Response: - 200: { "id": "uuid", "email": "user@example.com",
     * "emailVerified": true } - 401: { "message": "Invalid email or password" }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiSuccess<LoginResponse>> login(@RequestBody AuthLoginRequest request) {
        // Validate request
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        // Authenticate user
        UserEntity user = authService.authenticateUser(request.getEmail(), request.getPassword());
        if (user == null) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalStateException("Email verification is required before login");
        }
        if (twoFactorService.isEnabled(user.getId())) {
            if (request.getTwoFactorCode() == null || request.getTwoFactorCode().isBlank()) {
                throw new IllegalStateException("Two-factor code is required");
            }
            boolean validCode = twoFactorService.verifyCode(user.getId(), request.getTwoFactorCode().trim());
            if (!validCode) {
                throw new IllegalArgumentException("Invalid two-factor code");
            }
        }
        // Generate JWT access token
        String accessToken = authService.generateAccessToken(user);
        // Generate refresh token
        RefreshTokenEntity refreshToken = authService.generateRefreshToken(user);
        // Return tokens
        return ResponseEntity.ok(ApiSuccess.of(new LoginResponse(accessToken, refreshToken.getToken())));
    }
}
