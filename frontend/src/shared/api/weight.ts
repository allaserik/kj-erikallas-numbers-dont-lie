/**
 * Weight API
 * Handles weight entry (check-in) operations
 */
import { api } from "./client";
import type { WeightEntry, PaginatedResponse } from "../types";

/**
 * Record a weight check-in
 */
export function recordWeight(
    data: { weight: number; date?: string; notes?: string },
    token: string
): Promise<WeightEntry> {
    return api.post<WeightEntry>("/api/weight", data, token);
}

/**
 * Get latest weight entry
 */
export function getLatestWeight(token: string): Promise<WeightEntry | null> {
    return api.get<WeightEntry | null>("/api/weight/latest", token);
}

/**
 * Get weight history (paginated)
 */
export function getWeightHistory(
    params: { page?: number; size?: number; sort?: string },
    token: string
): Promise<PaginatedResponse<WeightEntry>> {
    const query = new URLSearchParams(
        Object.entries(params)
            .filter(([_, v]) => v !== undefined)
            .map(([k, v]) => [k, String(v)])
    );
    const queryStr = query.toString();
    const path = `/api/weight/history${queryStr ? "?" + queryStr : ""}`;
    return api.get<PaginatedResponse<WeightEntry>>(path, token);
}

/**
 * Get weight entries for a specific date range
 */
export function getWeightByDateRange(
    startDate: string,
    endDate: string,
    token: string
): Promise<WeightEntry[]> {
    const query = new URLSearchParams({ startDate, endDate }).toString();
    return api.get<WeightEntry[]>(`/api/weight/range?${query}`, token);
}

/**
 * Update weight entry
 */
export function updateWeightEntry(
    id: string,
    data: Partial<WeightEntry>,
    token: string
): Promise<WeightEntry> {
    return api.put<WeightEntry>(`/api/weight/${id}`, data, token);
}

/**
 * Delete weight entry
 */
export function deleteWeightEntry(id: string, token: string): Promise<void> {
    return api.del<void>(`/api/weight/${id}`, token);
}
