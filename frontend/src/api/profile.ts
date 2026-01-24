import { api } from "../shared/api/client";

export type ProfileResponse = {
  birthYear?: number | null;
  gender?: string | null;
  heightCm: number;
  baselineActivityLevel: "sedentary" | "light" | "moderate" | "active" | "very_active";
  bmiValue?: number | null;
  bmiClassification?: string | null;
  wellnessScore?: number | null;
  dietaryPreferences?: string[] | null;
  dietaryRestrictions?: string[] | null;
  fitnessAssessment?: {
    weekly_exercise_days?: number;
    exercise_preference?: string;
    current_fitness_level?: string;
  } | null;
  fitnessAssessmentCompleted?: boolean | null;
};

export type UpsertProfileRequest = {
  birthYear?: number | null;
  gender?: string | null;
  heightCm: number;
  baselineActivityLevel: "sedentary" | "light" | "moderate" | "active" | "very_active";
  dietaryPreferences?: string[] | null;
  dietaryRestrictions?: string[] | null;
  fitnessAssessment?: {
    weekly_exercise_days?: number;
    exercise_preference?: string;
    current_fitness_level?: string;
  } | null;
  fitnessAssessmentCompleted?: boolean | null;
};

export function getProfile(token: string) {
  return api.get<ProfileResponse>("/api/profile", token);
}

// Choose PUT for idempotent upsert (adjust if your backend uses POST)
export function upsertProfile(token: string, body: UpsertProfileRequest) {
  return api.put<ProfileResponse>("/api/profile", body, token);
}
