package com.erikallas.ndl.nutrition.preferences;

import com.erikallas.ndl.health.profile.HealthProfileRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NutritionPreferencesService {

    public record OptionItem(String code, String label) {
    }

    public record OptionsResponse(List<OptionItem> dietaryPreferences, List<OptionItem> allergies) {
    }

    public record PreferencesResponse(
            String timezone,
            Integer calorieTargetKcal,
            BigDecimal proteinTargetG,
            BigDecimal carbsTargetG,
            BigDecimal fatsTargetG,
            short mealsPerDay,
            short snacksPerDay,
            Map<String, Object> mealTimes,
            List<String> dietaryPreferenceCodes,
            List<String> allergyCodes,
            List<String> dislikedIngredients,
            List<String> cuisinePreferences,
            OffsetDateTime updatedAt) {
    }

    public record UpsertCommand(
            String timezone,
            Integer calorieTargetKcal,
            BigDecimal proteinTargetG,
            BigDecimal carbsTargetG,
            BigDecimal fatsTargetG,
            short mealsPerDay,
            short snacksPerDay,
            Map<String, Object> mealTimes,
            List<String> dietaryPreferenceCodes,
            List<String> allergyCodes,
            List<String> dislikedIngredients,
            List<String> cuisinePreferences) {
    }

    private final NutritionPreferencesRepository preferencesRepository;
    private final DietaryPreferenceOptionRepository dietaryOptionRepository;
    private final AllergyOptionRepository allergyOptionRepository;
    private final UserDietaryPreferenceRepository userDietaryPreferenceRepository;
    private final UserAllergyRepository userAllergyRepository;
    private final UserDislikedIngredientRepository userDislikedIngredientRepository;
    private final UserCuisinePreferenceRepository userCuisinePreferenceRepository;
    private final HealthProfileRepository healthProfileRepository;

    public NutritionPreferencesService(
            NutritionPreferencesRepository preferencesRepository,
            DietaryPreferenceOptionRepository dietaryOptionRepository,
            AllergyOptionRepository allergyOptionRepository,
            UserDietaryPreferenceRepository userDietaryPreferenceRepository,
            UserAllergyRepository userAllergyRepository,
            UserDislikedIngredientRepository userDislikedIngredientRepository,
            UserCuisinePreferenceRepository userCuisinePreferenceRepository,
            HealthProfileRepository healthProfileRepository) {
        this.preferencesRepository = preferencesRepository;
        this.dietaryOptionRepository = dietaryOptionRepository;
        this.allergyOptionRepository = allergyOptionRepository;
        this.userDietaryPreferenceRepository = userDietaryPreferenceRepository;
        this.userAllergyRepository = userAllergyRepository;
        this.userDislikedIngredientRepository = userDislikedIngredientRepository;
        this.userCuisinePreferenceRepository = userCuisinePreferenceRepository;
        this.healthProfileRepository = healthProfileRepository;
    }

    public OptionsResponse getOptions() {
        List<OptionItem> dietary = dietaryOptionRepository.findByActiveTrueOrderByLabelAsc().stream()
                .map(opt -> new OptionItem(opt.getCode(), opt.getLabel()))
                .toList();
        List<OptionItem> allergies = allergyOptionRepository.findByActiveTrueOrderByLabelAsc().stream()
                .map(opt -> new OptionItem(opt.getCode(), opt.getLabel()))
                .toList();
        return new OptionsResponse(dietary, allergies);
    }

    @Transactional
    public PreferencesResponse getOrCreateWithPrefill(UUID userId) {
        NutritionPreferencesEntity entity = preferencesRepository.findById(userId).orElseGet(() -> createDefault(userId));
        return toResponse(entity, userId);
    }

    @Transactional
    public PreferencesResponse upsert(UUID userId, UpsertCommand command) {
        validateTimezone(command.timezone());
        validateMealStructure(command.mealsPerDay(), command.snacksPerDay());

        NutritionPreferencesEntity entity = preferencesRepository.findById(userId).orElseGet(() -> createDefault(userId));
        entity.setTimezone(command.timezone());
        entity.setCalorieTargetKcal(command.calorieTargetKcal());
        entity.setProteinTargetG(command.proteinTargetG());
        entity.setCarbsTargetG(command.carbsTargetG());
        entity.setFatsTargetG(command.fatsTargetG());
        entity.setMealsPerDay(command.mealsPerDay());
        entity.setSnacksPerDay(command.snacksPerDay());
        entity.setMealTimesJson(command.mealTimes() != null ? command.mealTimes() : Map.of());
        entity.setUpdatedAt(OffsetDateTime.now());
        preferencesRepository.save(entity);

        applyDietaryCodes(userId, command.dietaryPreferenceCodes());
        applyAllergyCodes(userId, command.allergyCodes());
        applyDislikedIngredients(userId, command.dislikedIngredients());
        applyCuisinePreferences(userId, command.cuisinePreferences());

        return toResponse(entity, userId);
    }

    private NutritionPreferencesEntity createDefault(UUID userId) {
        OffsetDateTime now = OffsetDateTime.now();
        NutritionPreferencesEntity entity = new NutritionPreferencesEntity(
                userId,
                "UTC",
                null,
                null,
                null,
                null,
                (short) 3,
                (short) 0,
                Map.of(),
                now,
                now);
        preferencesRepository.save(entity);
        prefillFromHealthProfile(userId);
        return entity;
    }

    private PreferencesResponse toResponse(NutritionPreferencesEntity entity, UUID userId) {
        List<String> dietaryCodes = userDietaryPreferenceRepository.findByIdUserId(userId).stream()
                .map(p -> p.getId().getPreferenceCode())
                .sorted()
                .toList();
        List<String> allergyCodes = userAllergyRepository.findByIdUserId(userId).stream()
                .map(a -> a.getId().getAllergyCode())
                .sorted()
                .toList();
        List<String> disliked = userDislikedIngredientRepository.findByUserId(userId).stream()
                .map(UserDislikedIngredientEntity::getIngredientLabel)
                .sorted(String::compareToIgnoreCase)
                .toList();
        List<String> cuisines = userCuisinePreferenceRepository.findByUserId(userId).stream()
                .map(UserCuisinePreferenceEntity::getCuisineLabel)
                .sorted(String::compareToIgnoreCase)
                .toList();
        return new PreferencesResponse(
                entity.getTimezone(),
                entity.getCalorieTargetKcal(),
                entity.getProteinTargetG(),
                entity.getCarbsTargetG(),
                entity.getFatsTargetG(),
                entity.getMealsPerDay(),
                entity.getSnacksPerDay(),
                entity.getMealTimesJson(),
                dietaryCodes,
                allergyCodes,
                disliked,
                cuisines,
                entity.getUpdatedAt());
    }

    private void prefillFromHealthProfile(UUID userId) {
        var profileOpt = healthProfileRepository.findByUserId(userId);
        if (profileOpt.isEmpty()) {
            return;
        }
        var profile = profileOpt.get();
        List<String> profilePreferences = profile.getDietaryPreferences() != null ? profile.getDietaryPreferences() : List.of();
        List<String> profileRestrictions = profile.getDietaryRestrictions() != null ? profile.getDietaryRestrictions()
                : List.of();

        Set<String> activeDietaryCodes = dietaryOptionRepository.findByActiveTrueOrderByLabelAsc().stream()
                .map(DietaryPreferenceOptionEntity::getCode)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        Set<String> activeAllergyCodes = allergyOptionRepository.findByActiveTrueOrderByLabelAsc().stream()
                .map(AllergyOptionEntity::getCode)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        LinkedHashSet<String> mappedDietary = new LinkedHashSet<>();
        for (String item : profilePreferences) {
            String code = normalizeCode(item);
            if (activeDietaryCodes.contains(code)) {
                mappedDietary.add(code);
            }
        }

        LinkedHashSet<String> mappedAllergies = new LinkedHashSet<>();
        for (String item : profileRestrictions) {
            String code = mapRestrictionToAllergyCode(item);
            if (activeAllergyCodes.contains(code)) {
                mappedAllergies.add(code);
            }
        }

        applyDietaryCodes(userId, new ArrayList<>(mappedDietary));
        applyAllergyCodes(userId, new ArrayList<>(mappedAllergies));
    }

    private void applyDietaryCodes(UUID userId, List<String> rawCodes) {
        List<String> codes = sanitizeAndDeduplicate(rawCodes);
        Set<String> validCodes = dietaryOptionRepository.findByActiveTrueOrderByLabelAsc().stream()
                .map(DietaryPreferenceOptionEntity::getCode)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        for (String code : codes) {
            if (!validCodes.contains(code)) {
                throw new IllegalArgumentException("Invalid dietary preference code: " + code);
            }
        }

        userDietaryPreferenceRepository.deleteByIdUserId(userId);
        OffsetDateTime now = OffsetDateTime.now();
        List<UserDietaryPreferenceEntity> entries = codes.stream()
                .map(code -> new UserDietaryPreferenceEntity(new UserDietaryPreferenceId(userId, code), now))
                .toList();
        userDietaryPreferenceRepository.saveAll(entries);
    }

    private void applyAllergyCodes(UUID userId, List<String> rawCodes) {
        List<String> codes = sanitizeAndDeduplicate(rawCodes);
        Set<String> validCodes = allergyOptionRepository.findByActiveTrueOrderByLabelAsc().stream()
                .map(AllergyOptionEntity::getCode)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        for (String code : codes) {
            if (!validCodes.contains(code)) {
                throw new IllegalArgumentException("Invalid allergy code: " + code);
            }
        }

        userAllergyRepository.deleteByIdUserId(userId);
        OffsetDateTime now = OffsetDateTime.now();
        List<UserAllergyEntity> entries = codes.stream()
                .map(code -> new UserAllergyEntity(new UserAllergyId(userId, code), now))
                .toList();
        userAllergyRepository.saveAll(entries);
    }

    private void applyDislikedIngredients(UUID userId, List<String> rawValues) {
        List<String> values = sanitizeAndDeduplicate(rawValues);
        userDislikedIngredientRepository.deleteByUserId(userId);
        OffsetDateTime now = OffsetDateTime.now();
        List<UserDislikedIngredientEntity> rows = values.stream()
                .map(v -> new UserDislikedIngredientEntity(UUID.randomUUID(), userId, v, now))
                .toList();
        userDislikedIngredientRepository.saveAll(rows);
    }

    private void applyCuisinePreferences(UUID userId, List<String> rawValues) {
        List<String> values = sanitizeAndDeduplicate(rawValues);
        userCuisinePreferenceRepository.deleteByUserId(userId);
        OffsetDateTime now = OffsetDateTime.now();
        List<UserCuisinePreferenceEntity> rows = values.stream()
                .map(v -> new UserCuisinePreferenceEntity(UUID.randomUUID(), userId, v, now))
                .toList();
        userCuisinePreferenceRepository.saveAll(rows);
    }

    private List<String> sanitizeAndDeduplicate(List<String> values) {
        if (values == null) {
            return List.of();
        }
        LinkedHashSet<String> deduped = values.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(v -> v.trim())
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        return deduped.stream().sorted(Comparator.naturalOrder()).toList();
    }

    private String normalizeCode(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
        return normalized.replaceAll("^_+|_+$", "");
    }

    private String mapRestrictionToAllergyCode(String value) {
        String code = normalizeCode(value);
        Map<String, String> alias = new HashMap<>();
        alias.put("lactose", "lactose_intolerance");
        alias.put("lactose_intolerant", "lactose_intolerance");
        alias.put("nuts", "tree_nut");
        alias.put("shell_fish", "shellfish");
        alias.put("seafood", "fish");
        return alias.getOrDefault(code, code);
    }

    private void validateTimezone(String timezone) {
        try {
            ZoneId.of(timezone);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid timezone");
        }
    }

    private void validateMealStructure(short mealsPerDay, short snacksPerDay) {
        if (mealsPerDay < 1 || mealsPerDay > 8) {
            throw new IllegalArgumentException("meals_per_day must be between 1 and 8");
        }
        if (snacksPerDay < 0 || snacksPerDay > 6) {
            throw new IllegalArgumentException("snacks_per_day must be between 0 and 6");
        }
    }
}
