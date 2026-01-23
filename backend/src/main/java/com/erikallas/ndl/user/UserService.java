package com.erikallas.ndl.user;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final AppUserRepository repo;

    public UserService(AppUserRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public AppUserEntity ensureUser(String auth0Sub, String emailOrNull) {
        var user = repo.findByAuth0Sub(auth0Sub).orElseGet(() -> {
            // First login: create new user
            var newUser = new AppUserEntity(UUID.randomUUID(), auth0Sub, emailOrNull, OffsetDateTime.now());
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
}
