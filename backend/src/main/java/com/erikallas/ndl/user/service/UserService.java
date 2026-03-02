package com.erikallas.ndl.user.service;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import com.erikallas.ndl.user.model.UserEntity;
import com.erikallas.ndl.user.model.UserRepository;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public UserEntity ensureUser(String auth0Sub, String emailOrNull) {
        var user = repo.findByAuth0Sub(auth0Sub).orElseGet(() -> {
            // First login: create new user
            var newUser = new UserEntity(UUID.randomUUID(), auth0Sub, emailOrNull, OffsetDateTime.now());
            // Auth0 email is already verified
            if (emailOrNull != null) {
                newUser.setEmailVerified(true);
            }
            return repo.save(newUser);
        });

        // Sync email on every login (Auth0 may change user email)
        if (emailOrNull != null && !emailOrNull.equals(user.getEmail())) {
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
        return ensureUser(auth0Sub, email);
    }
}
