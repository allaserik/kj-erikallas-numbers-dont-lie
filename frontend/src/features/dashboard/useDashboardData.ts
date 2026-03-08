import { useAuthedQuery } from "../../shared/auth/useAuthedQuery";
import { getMe, getHealthProfile } from "../../shared/api/profile";
import { getLatestWeight } from "../../shared/api/weight";
import { getLatestInsight } from "../../shared/api/insights";
import { getActiveGoals } from "../../shared/api/goals";
import { getHealthSummary } from "../../shared/api/summary";
import { getWeeklySummary, getMonthlySummary } from "../../shared/api/summaries";
import { useAppAuth } from "../../shared/auth/AuthContext";
import type { UserProfile, HealthProfile, WeightEntry, Goal, Insight, HealthSummary, PeriodSummary } from "../../shared/types";

export interface DashboardData {
    me: UserProfile | null;
    profile: HealthProfile | null;
    latestWeight: WeightEntry | null;
    activeGoal: Goal | null;
    insight: Insight | null;
    summary: HealthSummary | null;
    weeklySummary: PeriodSummary | null;
    monthlySummary: PeriodSummary | null;
    insightConsentRequired: boolean;
}

export interface DashboardState extends DashboardData {
    isLoading: boolean;
    error: Error | null;
}

// Custom hook to orchestrate all dashboard data fetching
export function useDashboardData(): DashboardState {
    const { isAuthenticated } = useAppAuth();

    // Load individual data endpoints in parallel
    const meQ = useAuthedQuery("me", getMe, isAuthenticated);
    const profileQ = useAuthedQuery("profile", getHealthProfile, isAuthenticated);
    const latestWeightQ = useAuthedQuery("latestWeight", getLatestWeight, isAuthenticated);
    const goalsQ = useAuthedQuery("goals", getActiveGoals, isAuthenticated);
    const summaryQ = useAuthedQuery("summary", getHealthSummary, isAuthenticated);
    const weeklySummaryQ = useAuthedQuery("weeklySummary", getWeeklySummary, isAuthenticated);
    const monthlySummaryQ = useAuthedQuery("monthlySummary", getMonthlySummary, isAuthenticated);

    // Always fetch insights - backend returns generic fallback if goal/profile missing
    const insightQ = useAuthedQuery("latestInsight", getLatestInsight, isAuthenticated);

    // Determine overall loading state
    const isLoading =
        meQ.loading ||
        profileQ.loading ||
        latestWeightQ.loading ||
        goalsQ.loading ||
        summaryQ.loading ||
        weeklySummaryQ.loading ||
        monthlySummaryQ.loading ||
        insightQ.loading;

    // Determine overall error state (return first error found, ignore summary errors)
    const insightConsentRequired = (insightQ.error?.message || "").toLowerCase().includes("consent");

    const error =
        meQ.error ||
        profileQ.error ||
        latestWeightQ.error ||
        goalsQ.error ||
        (insightConsentRequired ? null : insightQ.error) ||
        null;

    // Extract data from individual queries
    const latestWeight = latestWeightQ.data;
    const activeGoal = goalsQ.data || null;

    return {
        me: meQ.data || null,
        profile: profileQ.data || null,
        latestWeight,
        activeGoal,
        insight: insightQ.data || null,
        summary: summaryQ.data || null,
        weeklySummary: weeklySummaryQ.data || null,
        monthlySummary: monthlySummaryQ.data || null,
        insightConsentRequired,
        isLoading,
        error,
    };
}
