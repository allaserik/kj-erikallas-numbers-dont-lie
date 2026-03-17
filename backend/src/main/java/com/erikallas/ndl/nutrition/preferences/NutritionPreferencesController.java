package com.erikallas.ndl.nutrition.preferences;

import com.erikallas.ndl.auth.user.UserService;
import com.erikallas.ndl.common.api.dto.ApiSuccess;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "bearerAuth")
@RestController
public class NutritionPreferencesController {

    private final UserService userService;
    private final NutritionPreferencesService nutritionPreferencesService;

    public NutritionPreferencesController(UserService userService, NutritionPreferencesService nutritionPreferencesService) {
        this.userService = userService;
        this.nutritionPreferencesService = nutritionPreferencesService;
    }

    public static class NutritionPreferencesRequest {
        @NotBlank
        @JsonProperty("timezone")
        public String timezone;

        @JsonProperty("calorie_target_kcal")
        public Integer calorieTargetKcal;

        @JsonProperty("protein_target_g")
        public BigDecimal proteinTargetG;

        @JsonProperty("carbs_target_g")
        public BigDecimal carbsTargetG;

        @JsonProperty("fats_target_g")
        public BigDecimal fatsTargetG;

        @NotNull
        @JsonProperty("meals_per_day")
        public Short mealsPerDay;

        @NotNull
        @JsonProperty("snacks_per_day")
        public Short snacksPerDay;

        @JsonProperty("meal_times")
        public Map<String, Object> mealTimes;

        @JsonProperty("dietary_preference_codes")
        public List<String> dietaryPreferenceCodes;

        @JsonProperty("allergy_codes")
        public List<String> allergyCodes;

        @JsonProperty("disliked_ingredients")
        public List<String> dislikedIngredients;

        @JsonProperty("cuisine_preferences")
        public List<String> cuisinePreferences;
    }

    @GetMapping("/api/nutrition/preferences/options")
    public ApiSuccess<NutritionPreferencesService.OptionsResponse> getOptions(JwtAuthenticationToken auth) {
        userService.ensureUserFromJwt(auth);
        return ApiSuccess.of(nutritionPreferencesService.getOptions());
    }

    @GetMapping("/api/nutrition/preferences")
    public ApiSuccess<NutritionPreferencesService.PreferencesResponse> getPreferences(JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        return ApiSuccess.of(nutritionPreferencesService.getOrCreateWithPrefill(user.getId()));
    }

    @PostMapping("/api/nutrition/preferences")
    public ApiSuccess<NutritionPreferencesService.PreferencesResponse> upsertPreferences(
            @Valid @RequestBody NutritionPreferencesRequest request,
            JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        var command = new NutritionPreferencesService.UpsertCommand(
                request.timezone,
                request.calorieTargetKcal,
                request.proteinTargetG,
                request.carbsTargetG,
                request.fatsTargetG,
                request.mealsPerDay,
                request.snacksPerDay,
                request.mealTimes,
                request.dietaryPreferenceCodes,
                request.allergyCodes,
                request.dislikedIngredients,
                request.cuisinePreferences);
        return ApiSuccess.of(nutritionPreferencesService.upsert(user.getId(), command));
    }
}
