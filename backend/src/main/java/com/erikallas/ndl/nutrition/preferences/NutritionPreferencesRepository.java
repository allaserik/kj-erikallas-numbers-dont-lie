package com.erikallas.ndl.nutrition.preferences;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionPreferencesRepository extends JpaRepository<NutritionPreferencesEntity, UUID> {
}
