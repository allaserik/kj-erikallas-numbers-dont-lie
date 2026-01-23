import { useMemo } from "react";
import { ApiError } from "../../shared/api/client";
import { useAuthedQuery } from "../../shared/auth/useAuthedQuery";
import { getProfile } from "../../api/profile";
import { getActiveGoal } from "../../api/goals";
import { getWeights } from "../../api/weight";

export type SetupStatus = {
    hasProfile: boolean;
    hasActiveGoal: boolean;
    hasWeight: boolean;
    isReady: boolean;
    missing: Array<"profile" | "goal" | "weight">;
};

function isMissingError(e: unknown): boolean {
    if (!(e instanceof ApiError)) return false;

    // depending on your backend, missing prereqs may appear as 404 or 400
    if (e.status === 404) return true;

    if (e.status === 400) {
        const msg = (e.bodyJson?.message ?? e.message ?? "").toLowerCase();
        if (msg.includes("required")) return true; // e.g. "Active goal required"
    }

    return false;
}

function isRealError(e: unknown): boolean {
    if (!(e instanceof ApiError)) return true; // non-api errors are real
    return e.status >= 500 || e.status === 401 || e.status === 403;
}

export function useSetupStatus(enabled: boolean) {
    const profileQ = useAuthedQuery("setup_profile", getProfile, enabled);
    const goalQ = useAuthedQuery("setup_goal", getActiveGoal, enabled);
    const weightsQ = useAuthedQuery("setup_weights", getWeights, enabled);

    const loading = profileQ.loading || goalQ.loading || weightsQ.loading;

    // Determine missing vs real error
    const profileMissing = !!profileQ.error && isMissingError(profileQ.error);
    const goalMissing = !!goalQ.error && isMissingError(goalQ.error);
    const weightMissing =
        (!!weightsQ.error && isMissingError(weightsQ.error)) ||
        (!!weightsQ.data && weightsQ.data.length === 0);

    const missing: SetupStatus["missing"] = [];
    if (profileMissing) missing.push("profile");
    if (goalMissing) missing.push("goal");
    if (weightMissing) missing.push("weight");

    const hasProfile = !profileMissing && !!profileQ.data;
    const hasActiveGoal = !goalMissing && !!goalQ.data;
    const hasWeight = !weightMissing && Array.isArray(weightsQ.data) && weightsQ.data.length > 0;

    const status: SetupStatus = useMemo(
        () => ({
            hasProfile,
            hasActiveGoal,
            hasWeight,
            isReady: hasProfile && hasActiveGoal && hasWeight,
            missing,
        }),
        [hasProfile, hasActiveGoal, hasWeight, missing.join("|")]
    );

    const realError =
        (profileQ.error && isRealError(profileQ.error) ? profileQ.error : null) ||
        (goalQ.error && isRealError(goalQ.error) ? goalQ.error : null) ||
        (weightsQ.error && isRealError(weightsQ.error) ? weightsQ.error : null);

    return { status, loading, error: realError };
}
