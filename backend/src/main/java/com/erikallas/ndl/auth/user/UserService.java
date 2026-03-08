package com.erikallas.ndl.auth.user;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.erikallas.ndl.auth.user.model.UserEntity;
import com.erikallas.ndl.auth.user.model.UserRepository;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository repo;
    private final Auth0UserInfoService auth0UserInfoService;
    private final ConcurrentHashMap<String, Object> userLocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> userInfoAttemptAt = new ConcurrentHashMap<>();

    public UserService(UserRepository repo, Auth0UserInfoService auth0UserInfoService) {
        this.repo = repo;
        this.auth0UserInfoService = auth0UserInfoService;
    }

    @Transactional
    public UserEntity ensureUser(String auth0Sub, String emailOrNull) {
        return ensureUser(auth0Sub, emailOrNull, emailOrNull != null ? Boolean.TRUE : null);
    }

    @Transactional
    public UserEntity ensureUser(String auth0Sub, String emailOrNull, Boolean emailVerifiedOrNull) {
        log.debug("ensureUser called: auth0Sub={}, email={}", auth0Sub, emailOrNull);
        Object lock = userLocks.computeIfAbsent(auth0Sub, ignored -> new Object());
        synchronized (lock) {
            try {
                // Local JWT tokens use subject = internal user UUID; never create a new user in this flow.
                try {
                    UUID localUserId = UUID.fromString(auth0Sub);
                    var localUser = repo.findById(localUserId).orElse(null);
                    if (localUser != null) {
                        if (emailOrNull != null && !emailOrNull.equals(localUser.getEmail())) {
                            localUser.setEmail(emailOrNull);
                            if (emailVerifiedOrNull != null) {
                                localUser.setEmailVerified(emailVerifiedOrNull);
                            }
                            localUser.setUpdatedAt(OffsetDateTime.now());
                            try {
                                repo.save(localUser);
                            } catch (DataIntegrityViolationException ex) {
                                // Do not fail requests when email is already used by another account.
                                log.warn("Skipping local user email sync due to unique constraint: userId={}, email={}",
                                        localUser.getId(), emailOrNull);
                            }
                        }
                        return localUser;
                    }
                } catch (IllegalArgumentException ignored) {
                    // Not a UUID subject, continue with OAuth subject flow.
                }

                UserEntity user = repo.findByAuth0Sub(auth0Sub).orElse(null);
                if (user == null) {
                    // First login: create new user. If concurrent requests race, recover by
                    // re-reading the row instead of failing.
                    log.info("Creating new user: auth0Sub={}, email={}", auth0Sub, emailOrNull);
                    var newUser = new UserEntity(UUID.randomUUID(), auth0Sub, emailOrNull, OffsetDateTime.now());
                    if (emailOrNull != null && emailVerifiedOrNull != null) {
                        newUser.setEmailVerified(emailVerifiedOrNull);
                    }
                    try {
                        user = repo.save(newUser);
                    } catch (DataIntegrityViolationException ex) {
                        user = repo.findByAuth0Sub(auth0Sub).orElse(null);
                        if (user == null && emailOrNull != null) {
                            var sameEmailUser = repo.findByEmailIgnoreCase(emailOrNull).orElse(null);
                            if (sameEmailUser != null
                                    && (sameEmailUser.getAuth0Sub() == null
                                            || auth0Sub.equals(sameEmailUser.getAuth0Sub()))) {
                                sameEmailUser.setAuth0Sub(auth0Sub);
                                if (emailVerifiedOrNull != null) {
                                    sameEmailUser.setEmailVerified(emailVerifiedOrNull);
                                }
                                sameEmailUser.setUpdatedAt(OffsetDateTime.now());
                                user = repo.save(sameEmailUser);
                            }
                        }
                        if (user == null) {
                            // Last fallback: persist OAuth identity without email and continue.
                            var fallbackUser = new UserEntity(UUID.randomUUID(), auth0Sub, null, OffsetDateTime.now());
                            fallbackUser.setEmailVerified(false);
                            try {
                                user = repo.save(fallbackUser);
                            } catch (DataIntegrityViolationException secondEx) {
                                user = repo.findByAuth0Sub(auth0Sub).orElseThrow(() -> secondEx);
                            }
                        }
                    }
                }

                boolean changed = false;
                if (emailOrNull != null && !emailOrNull.equals(user.getEmail())) {
                    log.info("Updating user email: auth0Sub={}, oldEmail={}, newEmail={}", auth0Sub, user.getEmail(),
                            emailOrNull);
                    user.setEmail(emailOrNull);
                    changed = true;
                }
                if (emailVerifiedOrNull != null && !Objects.equals(user.getEmailVerified(), emailVerifiedOrNull)) {
                    user.setEmailVerified(emailVerifiedOrNull);
                    changed = true;
                }
                if (changed) {
                    user.setUpdatedAt(OffsetDateTime.now());
                    try {
                        user = repo.save(user);
                    } catch (DataIntegrityViolationException ex) {
                        // Email may already belong to another account. Keep current user state
                        // instead of failing all parallel dashboard calls.
                        log.warn("Skipping user email sync due to unique constraint: auth0Sub={}, email={}", auth0Sub,
                                emailOrNull);
                        user = repo.findByAuth0Sub(auth0Sub).orElse(user);
                    }
                }

                return user;
            } finally {
                userLocks.remove(auth0Sub, lock);
            }
        }
    }

    /**
     * Convenience method: Extract auth0Sub and email from JWT, then ensure user
     * exists. Email is extracted from the "email" claim if present.
     */
    @Transactional
    public UserEntity ensureUserFromJwt(JwtAuthenticationToken auth) {
        String auth0Sub = auth.getToken().getSubject();
        Map<String, Object> claims = auth.getToken().getClaims();
        String issuer = auth.getToken().getClaimAsString("iss");
        UserEntity existingUser = repo.findByAuth0Sub(auth0Sub).orElse(null);

        String email = getClaimAsString(claims, "email");
        Boolean emailVerified = getClaimAsBoolean(claims, "email_verified");

        // Some Auth0 access tokens omit email/email_verified; try custom claim names.
        if (email == null) {
            for (var entry : claims.entrySet()) {
                if (entry.getKey().endsWith("/email") && entry.getValue() instanceof String value) {
                    email = value;
                    break;
                }
            }
        }
        if (emailVerified == null) {
            for (var entry : claims.entrySet()) {
                if (entry.getKey().endsWith("/email_verified") && entry.getValue() instanceof Boolean value) {
                    emailVerified = value;
                    break;
                }
            }
        }

        // Prefer stored user email to avoid /userinfo rate limits.
        if (email == null && existingUser != null && existingUser.getEmail() != null) {
            email = existingUser.getEmail();
            if (emailVerified == null) {
                emailVerified = existingUser.getEmailVerified();
            }
        }

        // Last fallback: call Auth0 /userinfo with the bearer token.
        if (email == null && shouldAttemptUserInfo(auth0Sub)) {
            userInfoAttemptAt.put(auth0Sub, System.currentTimeMillis());
            var userInfo = auth0UserInfoService.fetchUserInfo(auth.getToken().getTokenValue(), issuer);
            if (userInfo.email() != null) {
                email = userInfo.email();
                userInfoAttemptAt.remove(auth0Sub);
            }
            if (emailVerified == null) {
                emailVerified = userInfo.emailVerified();
            }
        }

        log.debug("ensureUserFromJwt called: sub={}, email={}, emailVerified={}", auth0Sub, email, emailVerified);
        return ensureUser(auth0Sub, email, emailVerified);
    }

    private String getClaimAsString(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        return value instanceof String ? (String) value : null;
    }

    private Boolean getClaimAsBoolean(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        return value instanceof Boolean ? (Boolean) value : null;
    }

    private boolean shouldAttemptUserInfo(String auth0Sub) {
        Long lastAttempt = userInfoAttemptAt.get(auth0Sub);
        if (lastAttempt == null) {
            return true;
        }
        return (System.currentTimeMillis() - lastAttempt) > 30_000;
    }
}
