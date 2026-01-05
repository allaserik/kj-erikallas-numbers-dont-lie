package com.erikallas.ndl.health.profile;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthProfileRepository extends JpaRepository<HealthProfileEntity, UUID> {
}
