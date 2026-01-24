import { api } from "../shared/api/client";
import { WellnessScoreResponse } from "./types";

export function getWellnessScore(token: string) {
    return api.get<WellnessScoreResponse>("/api/wellness-score", token);
}

export function calculateWellnessScore(token: string) {
    return api.post<WellnessScoreResponse>("/api/wellness-score/calculate", {}, token);
}
