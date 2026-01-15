import { api } from "../shared/api/client";

export type WeightEntry = {
  valueKg: number;
  measuredAt: string;
};

export function getWeights(token: string) {
  return api.get<WeightEntry[]>("/api/weight", token);
}
