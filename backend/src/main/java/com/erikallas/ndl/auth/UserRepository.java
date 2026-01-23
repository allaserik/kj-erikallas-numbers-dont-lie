package com.erikallas.ndl.auth;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for UserEntity.
 * 
 * Spring Data JPA automatically implements these methods: - save(UserEntity) -
 * create or update - findById(UUID) - get by ID - delete(UserEntity) - delete -
 * findAll() - get all users
 * 
 * Custom methods below are automatically implemented based on naming
 * convention:
 */
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    /**
     * Find user by email (case-insensitive) Used during login and registration
     */
    Optional<UserEntity> findByEmailIgnoreCase(String email);

    /**
     * Check if user with email already exists
     */
    boolean existsByEmailIgnoreCase(String email);
}
