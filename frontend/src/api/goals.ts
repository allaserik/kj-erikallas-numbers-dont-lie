import { api } from "../shared/api/client";

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

export function getActiveGoal(token: string) {
  return api.get<GoalResponse>("/api/goals/active", token);
}
