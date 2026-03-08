/**
 * Goals API
 * Handles fitness/wellness goal CRUD operations
 */
import { api } from "./client";
import type { Goal } from "../types";

/**
 * Transform backend goal response (snake_case) to frontend format (camelCase)
 */
function transformGoalResponse(data: any): Goal {
    return {
        id: data.id,
        userId: data.user_id,
        title: data.goal_type, // Use goal_type as title for display
        goalType: data.goal_type,
        targetValue: data.target_weight_kg,
        targetActivityDaysPerWeek: data.target_activity_days_per_week,
        targetDate: data.target_date || new Date(Date.now() + 90 * 24 * 60 * 60 * 1000).toISOString().split("T")[0],
        isActive: data.is_active,
        progress: data.progress || 0,
        description: data.notes,
        createdAt: data.created_at ? new Date(data.created_at).toISOString() : new Date().toISOString(),
        updatedAt: data.updated_at ? new Date(data.updated_at).toISOString() : new Date().toISOString(),
    };
}

/**
 * Transform Goal type to backend request format
 * Note: Backend CreateGoalRequest uses camelCase, not snake_case
 */
function transformGoalRequest(data: any): any {
    return {
        goalType: data.goalType,
        targetWeightKg: data.targetValue,
        targetActivityDaysPerWeek: data.targetActivityDaysPerWeek,
        notes: data.description,
    };
}

/**
 * Create a new goal
 */
export function createGoal(
    data: Omit<Goal, "id" | "userId" | "createdAt" | "updatedAt">,
    token: string
): Promise<Goal> {
    return api.post<any>("/api/goals", transformGoalRequest(data), token).then(transformGoalResponse);
}

/**
 * Get all goals for user
 */
export function getAllGoals(token: string): Promise<Goal[]> {
    return api.get<any[]>("/api/goals", token).then((goals) => {
        if (!Array.isArray(goals)) {
            console.warn("Expected array from /api/goals but got:", goals);
            return [];
        }
        return goals.map(transformGoalResponse);
    });
}

/**
 * Get active goals only - returns single active goal or null
 */
export function getActiveGoals(token: string): Promise<Goal | null> {
    return api.get<any>("/api/goals/active", token)
        .then((data) => {
            if (!data) return null;
            return transformGoalResponse(data);
        })
        .catch(() => null); // Return null if no active goal exists
}

/**
 * Get a specific goal by ID
 */
export function getGoal(id: string, token: string): Promise<Goal> {
    return api.get<any>(`/api/goals/${id}`, token).then(transformGoalResponse);
}

/**
 * Update a goal
 */
export function updateGoal(id: string, data: Partial<Goal>, token: string): Promise<Goal> {
    const updateData: any = {};
    if (data.goalType !== undefined) updateData.goalType = data.goalType;
    if (data.targetValue !== undefined) updateData.targetWeightKg = data.targetValue;
    if (data.targetActivityDaysPerWeek !== undefined) updateData.targetActivityDaysPerWeek = data.targetActivityDaysPerWeek;
    if (data.description !== undefined) updateData.notes = data.description;
    if (data.isActive !== undefined) updateData.isActive = data.isActive;
    return api.patch<any>(`/api/goals/${id}`, updateData, token).then(transformGoalResponse);
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
