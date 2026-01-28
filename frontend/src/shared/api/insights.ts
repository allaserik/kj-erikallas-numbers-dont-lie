/**
 * Insights API
 * Handles AI-generated wellness insights
 */
import { api } from "./client";
import type { Insight } from "../types";

/**
 * Get latest AI insight
 */
export function getLatestInsight(token: string): Promise<Insight | null> {
    return api.get<Insight | null>("/api/insights/latest", token);
}

/**
 * Get all insights for user
 */
export function getAllInsights(token: string): Promise<Insight[]> {
    return api.get<Insight[]>("/api/insights", token);
}

/**
 * Get insights within a date range
 */
export function getInsightsByDateRange(
    startDate: string,
    endDate: string,
    token: string
): Promise<Insight[]> {
    const query = new URLSearchParams({ startDate, endDate }).toString();
    return api.get<Insight[]>(`/api/insights/range?${query}`, token);
}

/**
 * Trigger AI insight generation (manual trigger)
 */
export function generateInsight(token: string): Promise<Insight> {
    return api.post<Insight>("/api/insights/generate", {}, token);
}
