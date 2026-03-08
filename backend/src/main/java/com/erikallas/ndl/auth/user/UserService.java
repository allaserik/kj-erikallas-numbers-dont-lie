package com.erikallas.ndl.auth.user;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.erikallas.ndl.auth.user.model.UserEntity;
import com.erikallas.ndl.auth.user.model.UserRepository;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public UserEntity ensureUser(String auth0Sub, String emailOrNull) {
        log.debug("ensureUser called: auth0Sub={}, email={}", auth0Sub, emailOrNull);

        // Local JWT tokens use subject = internal user UUID; never create a new user in this flow.
        try {
            UUID localUserId = UUID.fromString(auth0Sub);
            var localUser = repo.findById(localUserId).orElse(null);
            if (localUser != null) {
                if (emailOrNull != null && !emailOrNull.equals(localUser.getEmail())) {
                    localUser.setEmail(emailOrNull);
                    localUser.setUpdatedAt(OffsetDateTime.now());
                    repo.save(localUser);
                }
                return localUser;
            }
        } catch (IllegalArgumentException ignored) {
            // Not a UUID subject, continue with OAuth subject flow.
        }

        var user = repo.findByAuth0Sub(auth0Sub).orElseGet(() -> {
            // First login: create new user
            log.info("Creating new user: auth0Sub={}, email={}", auth0Sub, emailOrNull);
            var newUser = new UserEntity(UUID.randomUUID(), auth0Sub, emailOrNull, OffsetDateTime.now());
            // Auth0 email is already verified
            if (emailOrNull != null) {
                newUser.setEmailVerified(true);
            }
            return repo.save(newUser);
        });

        // Sync email on every login (Auth0 may change user email)
        if (emailOrNull != null && !emailOrNull.equals(user.getEmail())) {
            log.info("Updating user email: auth0Sub={}, oldEmail={}, newEmail={}", auth0Sub, user.getEmail(),
                    emailOrNull);
            user.setEmail(emailOrNull);
            user.setEmailVerified(true);
            user.setUpdatedAt(OffsetDateTime.now());
            return repo.save(user);
        }

        return user;
    }

    /**
     * Convenience method: Extract auth0Sub and email from JWT, then ensure user
     * exists. Email is extracted from the "email" claim if present.
     */
    @Transactional
    public UserEntity ensureUserFromJwt(JwtAuthenticationToken auth) {
        String auth0Sub = auth.getToken().getSubject();
        String email = auth.getToken().getClaimAsString("email");
        log.info("ensureUserFromJwt called: sub={}, email={}", auth0Sub, email);
        return ensureUser(auth0Sub, email);
    }
}
