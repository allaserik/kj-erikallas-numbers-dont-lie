package com.erikallas.ndl.nutrition.preferences;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "nutrition_preferences")
public class NutritionPreferencesEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "timezone", nullable = false)
    private String timezone;

    @Column(name = "calorie_target_kcal")
    private Integer calorieTargetKcal;

    @Column(name = "protein_target_g")
    private BigDecimal proteinTargetG;

    @Column(name = "carbs_target_g")
    private BigDecimal carbsTargetG;

    @Column(name = "fats_target_g")
    private BigDecimal fatsTargetG;

    @Column(name = "meals_per_day", nullable = false)
    private short mealsPerDay;

    @Column(name = "snacks_per_day", nullable = false)
    private short snacksPerDay;

    @Column(name = "meal_times_json", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> mealTimesJson = new HashMap<>();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected NutritionPreferencesEntity() {
    }

    public NutritionPreferencesEntity(UUID userId, String timezone, Integer calorieTargetKcal, BigDecimal proteinTargetG,
            BigDecimal carbsTargetG, BigDecimal fatsTargetG, short mealsPerDay, short snacksPerDay,
            Map<String, Object> mealTimesJson, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.userId = userId;
        this.timezone = timezone;
        this.calorieTargetKcal = calorieTargetKcal;
        this.proteinTargetG = proteinTargetG;
        this.carbsTargetG = carbsTargetG;
        this.fatsTargetG = fatsTargetG;
        this.mealsPerDay = mealsPerDay;
        this.snacksPerDay = snacksPerDay;
        this.mealTimesJson = mealTimesJson != null ? mealTimesJson : new HashMap<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Integer getCalorieTargetKcal() {
        return calorieTargetKcal;
    }

    public void setCalorieTargetKcal(Integer calorieTargetKcal) {
        this.calorieTargetKcal = calorieTargetKcal;
    }

    public BigDecimal getProteinTargetG() {
        return proteinTargetG;
    }

    public void setProteinTargetG(BigDecimal proteinTargetG) {
        this.proteinTargetG = proteinTargetG;
    }

    public BigDecimal getCarbsTargetG() {
        return carbsTargetG;
    }

    public void setCarbsTargetG(BigDecimal carbsTargetG) {
        this.carbsTargetG = carbsTargetG;
    }

    public BigDecimal getFatsTargetG() {
        return fatsTargetG;
    }

    public void setFatsTargetG(BigDecimal fatsTargetG) {
        this.fatsTargetG = fatsTargetG;
    }

    public short getMealsPerDay() {
        return mealsPerDay;
    }

    public void setMealsPerDay(short mealsPerDay) {
        this.mealsPerDay = mealsPerDay;
    }

    public short getSnacksPerDay() {
        return snacksPerDay;
    }

    public void setSnacksPerDay(short snacksPerDay) {
        this.snacksPerDay = snacksPerDay;
    }

    public Map<String, Object> getMealTimesJson() {
        return mealTimesJson;
    }

    public void setMealTimesJson(Map<String, Object> mealTimesJson) {
        this.mealTimesJson = mealTimesJson != null ? mealTimesJson : new HashMap<>();
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
