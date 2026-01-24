import { api } from "../shared/api/client";
import type { GoalProgressResponse } from "./types";

export type GoalResponse = {
  id: string;
  userId: string;
  goalType: "WEIGHT_LOSS" | "MAINTAIN_WEIGHT" | "WEIGHT_GAIN" | "IMPROVE_FITNESS" | "BUILD_MUSCLE" | "ENHANCE_ENDURANCE" | "IMPROVE_FLEXIBILITY" | "REDUCE_STRESS" | "BETTER_SLEEP";
  targetWeightKg?: number;
  targetActivityDaysPerWeek?: number;
  notes?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
};

export type CreateGoalRequest = {
  goalType: GoalResponse["goalType"];
  targetWeightKg?: number;
  targetActivityDaysPerWeek?: number;
  notes?: string;
};

export function getActiveGoal(token: string) {
  return api.get<GoalResponse>("/api/goals/active", token);
}

export function createGoal(token: string, body: CreateGoalRequest) {
  return api.post<GoalResponse>("/api/goals", body, token);
}

export function getGoalProgress(token: string, goalId: string) {
  return api.get<GoalProgressResponse>(`/api/goals/${goalId}/progress`, token);
}

export function getGoalProgressHistory(token: string, goalId: string) {
  return api.get<GoalProgressResponse[]>(`/api/goals/${goalId}/progress/history`, token);
}

export function recordGoalProgress(token: string, goalId: string, currentValue: number) {
  return api.post<GoalProgressResponse>(`/api/goals/${goalId}/progress/record?currentValue=${currentValue}`, {}, token);
}
