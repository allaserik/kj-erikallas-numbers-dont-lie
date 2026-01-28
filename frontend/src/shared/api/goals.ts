/**
 * Goals API
 * Handles fitness/wellness goal CRUD operations
 */
import { api } from "./client";
import type { Goal } from "../types";

/**
 * Create a new goal
 */
export function createGoal(
    data: Omit<Goal, "id" | "userId" | "createdAt" | "updatedAt">,
    token: string
): Promise<Goal> {
    return api.post<Goal>("/api/goals", data, token);
}

/**
 * Get all goals for user
 */
export function getAllGoals(token: string): Promise<Goal[]> {
    return api.get<Goal[]>("/api/goals", token);
}

/**
 * Get active goals only
 */
export function getActiveGoals(token: string): Promise<Goal[]> {
    return api.get<Goal[]>("/api/goals?active=true", token);
}

/**
 * Get a specific goal by ID
 */
export function getGoal(id: string, token: string): Promise<Goal> {
    return api.get<Goal>(`/api/goals/${id}`, token);
}

/**
 * Update a goal
 */
export function updateGoal(id: string, data: Partial<Goal>, token: string): Promise<Goal> {
    return api.put<Goal>(`/api/goals/${id}`, data, token);
}

/**
 * Archive (deactivate) a goal
 */
export function archiveGoal(id: string, token: string): Promise<Goal> {
    return updateGoal(id, { isActive: false }, token);
}

/**
 * Delete a goal
 */
export function deleteGoal(id: string, token: string): Promise<void> {
    return api.del<void>(`/api/goals/${id}`, token);
}
