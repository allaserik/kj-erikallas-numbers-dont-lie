/**
 * Profile API
 * Handles health profile CRUD and user profile retrieval
 */
import { api } from "./client";
import type { UserProfile, HealthProfile } from "../types";

// Backend response structure (snake_case)
type HealthProfileBackendResponse = {
    user_id: string;
    birth_year: number;
    gender: string;
    height_cm: number;
    baseline_activity_level: string;
    dietary_preferences: string[];
    dietary_restrictions: string[];
    fitness_assessment: Record<string, unknown> | null;
    fitness_assessment_completed: boolean;
    wellness_score: number;
    bmi_value: number;
    bmi_classification: string;
    created_at: string;
    updated_at: string;
};

// Transform backend response to frontend type
function transformHealthProfile(data: HealthProfileBackendResponse | null): HealthProfile | null {
    if (!data) return null;

    const currentYear = new Date().getFullYear();
    const age = currentYear - data.birth_year;

    // Parse activity level: backend stores as "sedentary", "light active", etc.
    // Convert to SCREAMING_SNAKE_CASE
    let activityLevel: "SEDENTARY" | "LIGHTLY_ACTIVE" | "MODERATELY_ACTIVE" | "VERY_ACTIVE" | "EXTREMELY_ACTIVE" = "MODERATELY_ACTIVE";
    const levelStr = data.baseline_activity_level?.toLowerCase() || "";
    if (levelStr.includes("sedentary")) activityLevel = "SEDENTARY";
    else if (levelStr.includes("light")) activityLevel = "LIGHTLY_ACTIVE";
    else if (levelStr.includes("moderate")) activityLevel = "MODERATELY_ACTIVE";
    else if (levelStr.includes("very")) activityLevel = "VERY_ACTIVE";
    else if (levelStr.includes("extremely")) activityLevel = "EXTREMELY_ACTIVE";

    return {
        userId: data.user_id,
        height: data.height_cm,
        weight: 0, // Backend doesn't return current weight in profile endpoint
        age,
        gender: data.gender as "MALE" | "FEMALE" | "OTHER",
        activityLevel,
        targetWeight: 0,
        wellnessScore: data.wellness_score || 0,
        dietaryPreferences: data.dietary_preferences || [],
        fitnessGoals: [],
        createdAt: data.created_at,
        updatedAt: data.updated_at,
    };
}

/**
 * Get current user's profile (from Auth0/JWT)
 */
export function getMe(token: string): Promise<UserProfile> {
    return api.get<UserProfile>("/api/me", token);
}

/**
 * Get user's health profile (height, weight, age, goals, etc.)
 */
export async function getHealthProfile(token: string): Promise<HealthProfile | null> {
    const response = await api.get<HealthProfileBackendResponse | null>("/api/profile", token);
    return transformHealthProfile(response);
}

/**
 * Create or update user's health profile
 */
export async function upsertHealthProfile(
    data: unknown,
    token: string
): Promise<HealthProfile> {
    const response = await api.post<HealthProfileBackendResponse>("/api/profile", data, token);
    return transformHealthProfile(response)!;
}

/**
 * Update specific fields of health profile
 */
export async function updateHealthProfile(
    data: unknown,
    token: string
): Promise<HealthProfile> {
    const response = await api.post<HealthProfileBackendResponse>("/api/profile", data, token);
    return transformHealthProfile(response)!;
}
