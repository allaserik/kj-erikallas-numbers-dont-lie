import { useAuthedQuery } from "../../shared/auth/useAuthedQuery";
import { getWeightHistory } from "../../shared/api/weight";
import { getHealthProfile } from "../../shared/api/profile";
import type { WeightEntry, PaginatedResponse } from "../../shared/types";
import { useAppAuth } from "../../shared/auth/AuthContext";

export interface ChartDataPoint {
    date: string; // YYYY-MM-DD
    weight: number; // kg
    daysAgo: number;
}

export interface WeightChartState {
    points: ChartDataPoint[];
    targetWeight: number | null;
    minWeight: number;
    maxWeight: number;
    isLoading: boolean;
    error: Error | null;
}

export function useWeightChartData(): WeightChartState {
    const { isAuthenticated } = useAppAuth();

    // Fetch weight history (last 90 days)
    const weightQ = useAuthedQuery(
        "weightHistory",
        (token: string) => getWeightHistory({ size: 90 }, token),
        isAuthenticated
    );

    // Fetch health profile for target weight
    const profileQ = useAuthedQuery("profile", getHealthProfile, isAuthenticated);

    // Process weight data into chart points
    const points: ChartDataPoint[] = [];
    let minWeight = Infinity;
    let maxWeight = -Infinity;

    if (weightQ.data) {
        const entries = (weightQ.data as unknown as PaginatedResponse<WeightEntry>).content || [];

        // Sort by date ascending for proper plotting
        const sorted = [...entries].sort(
            (a, b) => new Date(a.date).getTime() - new Date(b.date).getTime()
        );

        const today = new Date();
        today.setHours(0, 0, 0, 0);

        sorted.forEach((entry) => {
            const entryDate = new Date(entry.date);
            entryDate.setHours(0, 0, 0, 0);
            const daysAgo = Math.floor(
                (today.getTime() - entryDate.getTime()) / (1000 * 60 * 60 * 24)
            );

            points.push({
                date: entry.date,
                weight: entry.weight,
                daysAgo,
            });

            minWeight = Math.min(minWeight, entry.weight);
            maxWeight = Math.max(maxWeight, entry.weight);
        });
    }

    // Add 10% padding to min/max for chart spacing
    const padding = (maxWeight - minWeight) * 0.1 || 5;
    const adjustedMin = minWeight === Infinity ? 70 : Math.floor(minWeight - padding);
    const adjustedMax = maxWeight === -Infinity ? 80 : Math.ceil(maxWeight + padding);

    return {
        points,
        targetWeight: profileQ.data?.targetWeight || null,
        minWeight: adjustedMin,
        maxWeight: adjustedMax,
        isLoading: weightQ.loading || profileQ.loading,
        error: weightQ.error || profileQ.error || null,
    };
}
