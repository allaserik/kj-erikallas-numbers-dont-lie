package com.erikallas.ndl.nutrition.preferences;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllergyOptionRepository extends JpaRepository<AllergyOptionEntity, String> {
    List<AllergyOptionEntity> findByActiveTrueOrderByLabelAsc();
}
