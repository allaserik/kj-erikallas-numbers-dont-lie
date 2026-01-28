/**
 * Profile API
 * Handles health profile CRUD and user profile retrieval
 */
import { api } from "./client";
import type { UserProfile, HealthProfile } from "../types";

/**
 * Get current user's profile (from Auth0/JWT)
 */
export function getMe(token: string): Promise<UserProfile> {
    return api.get<UserProfile>("/api/me", token);
}

/**
 * Get user's health profile (height, weight, age, goals, etc.)
 */
export function getHealthProfile(token: string): Promise<HealthProfile> {
    return api.get<HealthProfile>("/api/profile", token);
}

/**
 * Create or update user's health profile
 */
export function upsertHealthProfile(
    data: Partial<HealthProfile>,
    token: string
): Promise<HealthProfile> {
    return api.post<HealthProfile>("/api/profile", data, token);
}

/**
 * Update specific fields of health profile
 */
export function updateHealthProfile(
    data: Partial<HealthProfile>,
    token: string
): Promise<HealthProfile> {
    return api.put<HealthProfile>("/api/profile", data, token);
}
