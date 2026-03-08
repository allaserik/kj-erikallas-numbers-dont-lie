import { api } from "./client";
import type { ApiResponse } from "../types";
import { unwrapApiData } from "./unwrap";

export type WellnessScore = {
    score: number;
    description: string;
};

type WellnessScoreBackendResponse = {
    score: number;
    description: string;
};

function transformWellnessScore(data: WellnessScoreBackendResponse): WellnessScore {
    return {
        score: data.score,
        description: data.description,
    };
}

export function getWellnessScore(token: string): Promise<WellnessScore> {
    return api
        .get<WellnessScoreBackendResponse | ApiResponse<WellnessScoreBackendResponse>>("/api/wellness-score", token)
        .then((response) => transformWellnessScore(unwrapApiData(response)));
}

export function calculateWellnessScore(token: string): Promise<WellnessScore> {
    return api
        .post<WellnessScoreBackendResponse | ApiResponse<WellnessScoreBackendResponse>>(
            "/api/wellness-score/calculate",
            {},
            token
        )
        .then((response) => transformWellnessScore(unwrapApiData(response)));
}
