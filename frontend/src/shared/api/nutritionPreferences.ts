import { api } from "./client";
import type { ApiResponse } from "../types";
import { unwrapApiData } from "./unwrap";

export type NutritionOption = {
    code: string;
    label: string;
};

export type NutritionOptionsResponse = {
    dietaryPreferences: NutritionOption[];
    allergies: NutritionOption[];
};

export type NutritionPreferences = {
    timezone: string;
    calorieTargetKcal: number | null;
    proteinTargetG: number | null;
    carbsTargetG: number | null;
    fatsTargetG: number | null;
    mealsPerDay: number;
    snacksPerDay: number;
    mealTimes: Record<string, unknown>;
    dietaryPreferenceCodes: string[];
    allergyCodes: string[];
    dislikedIngredients: string[];
    cuisinePreferences: string[];
    updatedAt: string;
};

export type UpsertNutritionPreferencesRequest = {
    timezone: string;
    calorie_target_kcal: number | null;
    protein_target_g: number | null;
    carbs_target_g: number | null;
    fats_target_g: number | null;
    meals_per_day: number;
    snacks_per_day: number;
    meal_times: Record<string, unknown>;
    dietary_preference_codes: string[];
    allergy_codes: string[];
    disliked_ingredients: string[];
    cuisine_preferences: string[];
};

type NutritionPreferencesBackend = {
    timezone?: string;
    calorieTargetKcal?: number | null;
    proteinTargetG?: number | null;
    carbsTargetG?: number | null;
    fatsTargetG?: number | null;
    mealsPerDay?: number;
    snacksPerDay?: number;
    mealTimes?: Record<string, unknown>;
    dietaryPreferenceCodes?: string[];
    allergyCodes?: string[];
    dislikedIngredients?: string[];
    cuisinePreferences?: string[];
    updatedAt?: string;
};

function mapBackendPreferences(data: NutritionPreferencesBackend): NutritionPreferences {
    return {
        timezone: data.timezone || "UTC",
        calorieTargetKcal: data.calorieTargetKcal ?? null,
        proteinTargetG: data.proteinTargetG ?? null,
        carbsTargetG: data.carbsTargetG ?? null,
        fatsTargetG: data.fatsTargetG ?? null,
        mealsPerDay: data.mealsPerDay ?? 3,
        snacksPerDay: data.snacksPerDay ?? 0,
        mealTimes: data.mealTimes || {},
        dietaryPreferenceCodes: data.dietaryPreferenceCodes ?? [],
        allergyCodes: data.allergyCodes ?? [],
        dislikedIngredients: data.dislikedIngredients ?? [],
        cuisinePreferences: data.cuisinePreferences ?? [],
        updatedAt: data.updatedAt ?? new Date().toISOString(),
    };
}

export function getNutritionOptions(token: string): Promise<NutritionOptionsResponse> {
    return api
        .get<NutritionOptionsResponse | ApiResponse<NutritionOptionsResponse>>("/api/nutrition/preferences/options", token)
        .then((response) => unwrapApiData(response));
}

export function getNutritionPreferences(token: string): Promise<NutritionPreferences> {
    return api
        .get<NutritionPreferencesBackend | ApiResponse<NutritionPreferencesBackend>>("/api/nutrition/preferences", token)
        .then((response) => mapBackendPreferences(unwrapApiData(response)));
}

export function upsertNutritionPreferences(
    data: UpsertNutritionPreferencesRequest,
    token: string
): Promise<NutritionPreferences> {
    return api
        .post<NutritionPreferencesBackend | ApiResponse<NutritionPreferencesBackend>>("/api/nutrition/preferences", data, token)
        .then((response) => mapBackendPreferences(unwrapApiData(response)));
}
