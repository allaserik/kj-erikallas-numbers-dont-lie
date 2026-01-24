import { api } from "../shared/api/client";

export type WeightEntry = {
  weightKg: number;
  measuredAt: string;
};

export type AddWeightRequest = {
  weightKg: number;
  measuredAt?: string; // ISO 8601, defaults to now
};

export function getWeights(token: string) {
  return api.get<WeightEntry[]>("/api/weight", token);
}

export function addWeight(token: string, body: AddWeightRequest) {
  return api.post<WeightEntry>("/api/weight", body, token);
}
