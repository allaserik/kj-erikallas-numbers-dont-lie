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
        return repo.findByAuth0Sub(auth0Sub)
                .orElseGet(() -> repo.save(new AppUserEntity(
                        UUID.randomUUID(),
                        auth0Sub,
                        emailOrNull,
                        OffsetDateTime.now())));
    }
}
