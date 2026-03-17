package com.erikallas.ndl.nutrition.preferences;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DietaryPreferenceOptionRepository extends JpaRepository<DietaryPreferenceOptionEntity, String> {
    List<DietaryPreferenceOptionEntity> findByActiveTrueOrderByLabelAsc();
}
