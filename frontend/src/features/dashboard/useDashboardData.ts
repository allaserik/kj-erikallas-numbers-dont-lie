import { useAuthedQuery } from "../../shared/auth/useAuthedQuery";
import { getMe, getHealthProfile } from "../../shared/api/profile";
import { getLatestWeight } from "../../shared/api/weight";
import { getLatestInsight } from "../../shared/api/insights";
import { getActiveGoals } from "../../shared/api/goals";
import { getCurrentGoalProgress } from "../../shared/api/goalProgress";
import { getHealthSummary } from "../../shared/api/summary";
import { getWeeklySummary, getMonthlySummary } from "../../shared/api/summaries";
import { getPrivacyPreferences } from "../../shared/api/privacy";
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

    // Step 1: Establish user context first.
    const meQ = useAuthedQuery("me", getMe, isAuthenticated);
    const meReady = isAuthenticated && !!meQ.data && !meQ.loading && !meQ.error;

    // Step 2: Fetch independent user resources after /me succeeds.
    const profileQ = useAuthedQuery("profile", getHealthProfile, meReady);
    const latestWeightQ = useAuthedQuery("latestWeight", getLatestWeight, meReady);
    const privacyQ = useAuthedQuery("privacyPreferences", getPrivacyPreferences, meReady);
    const goalsQ = useAuthedQuery("goals", getActiveGoals, meReady);
    const activeGoalId = goalsQ.data?.id;
    const goalProgressQ = useAuthedQuery(
        "activeGoalProgress",
        (token: string) => getCurrentGoalProgress(activeGoalId as string, token),
        meReady && !!activeGoalId
    );
    const hasProfile = !!profileQ.data;
    const hasWeight = !!latestWeightQ.data;
    const hasConsent = !!privacyQ.data?.data_usage_consent;

    // Step 3: Fetch dependent resources only when prerequisites are present.
    const summaryQ = useAuthedQuery("summary", getHealthSummary, meReady && hasProfile && hasWeight);
    const weeklySummaryQ = useAuthedQuery("weeklySummary", getWeeklySummary, meReady && hasProfile && hasWeight);
    const monthlySummaryQ = useAuthedQuery("monthlySummary", getMonthlySummary, meReady && hasProfile && hasWeight);

    // Insights require explicit data usage consent and a completed profile context.
    const insightQ = useAuthedQuery("latestInsight", getLatestInsight, meReady && hasProfile && hasConsent);

    // Determine overall loading state
    const isLoading =
        meQ.loading ||
        profileQ.loading ||
        latestWeightQ.loading ||
        privacyQ.loading ||
        goalsQ.loading ||
        goalProgressQ.loading ||
        summaryQ.loading ||
        weeklySummaryQ.loading ||
        monthlySummaryQ.loading ||
        insightQ.loading;

    // Determine overall error state (return first error found, ignore summary errors)
    const insightConsentRequired =
        (hasProfile && !hasConsent) || (insightQ.error?.message || "").toLowerCase().includes("consent");

    const error =
        meQ.error ||
        profileQ.error ||
        latestWeightQ.error ||
        privacyQ.error ||
        goalsQ.error ||
        goalProgressQ.error ||
        (insightConsentRequired ? null : insightQ.error) ||
        null;

    // Extract data from individual queries
    const latestWeight = latestWeightQ.data;
    const activeGoal = goalsQ.data
        ? {
            ...goalsQ.data,
            progress:
                goalProgressQ.data?.progressPercentage ??
                goalsQ.data.progress ??
                0,
        }
        : null;

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
