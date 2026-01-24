import { useAuth0 } from "@auth0/auth0-react";
import { PageHeader } from "../../components/layout/PageHeader";
import { Button } from "../../shared/ui/Button";
import { Alert } from "../../shared/ui/Alert";
import { Card, CardBody, CardSubtitle, CardTitle } from "../../shared/ui/Card";
import { Spinner } from "../../shared/ui/Spinner";
import { useAuthedQuery } from "../../shared/auth/useAuthedQuery";
import { getMe } from "../../api/me";
import { getProfile } from "../../api/profile";
import { getActiveGoal, getGoalProgress } from "../../api/goals";
import { getWeights } from "../../api/weight";
import { getCurrentInsight } from "../../api/insights";
import { getWellnessScore } from "../../api/wellness";
import { explainApiError } from "../../shared/api/errors";
import { isSetupRequired } from "../setup/isSetupRequired";
import { Link } from "react-router-dom";
import { SetupRequiredCard } from "../setup/components/SetupRequiredCard";
import { DashboardErrorAlert } from "./components/DashboardErrorAlert";
import { DashboardLoadingCard } from "./components/DashboardLoadingCard";



export default function Dashboard() {
    const { loginWithRedirect, logout, isAuthenticated, isLoading, user } = useAuth0();

    const meQ = useAuthedQuery("me", getMe);
    const profileQ = useAuthedQuery("profile", getProfile);
    const goalQ = useAuthedQuery("activeGoal", getActiveGoal);
    const weightsQ = useAuthedQuery("weights", getWeights);
    const insightQ = useAuthedQuery("currentInsight", getCurrentInsight);
    const wellnessQ = useAuthedQuery("wellness", getWellnessScore);

    const progressQ = useAuthedQuery(
        goalQ.data ? `goalProgress-${goalQ.data.id}` : null,
        goalQ.data ? () => {
            const token = localStorage.getItem("token");
            if (!token) throw new Error("No token");
            return getGoalProgress(token, goalQ.data!.id);
        } : null,
        goalQ.data ? undefined : { skip: true }
    );

    const setupRequired = isSetupRequired(profileQ.error);

    const anyLoading =
        meQ.loading || profileQ.loading || goalQ.loading || weightsQ.loading || insightQ.loading || wellnessQ.loading;

    const firstError =
        meQ.error || profileQ.error || goalQ.error || weightsQ.error || insightQ.error || wellnessQ.error;

    const firstErrorMessage = firstError ? explainApiError(firstError) : "";

    const latestWeight = weightsQ.data?.[0]?.weightKg;
// ...existing code...
// ...existing code from pages/Dashboard.tsx will be moved here
