import { useAuth0 } from "@auth0/auth0-react";
import { PageHeader } from "../components/layout/PageHeader"; // if you don't have this yet, we’ll add below
import { Button } from "../shared/ui/Button";
import { Alert } from "../shared/ui/Alert";
import { Card, CardBody, CardSubtitle, CardTitle } from "../shared/ui/Card";
import { Spinner } from "../shared/ui/Spinner";

import { useAuthedQuery } from "../shared/auth/useAuthedQuery";
import { getMe } from "../api/me";
import { getProfile } from "../api/profile";
import { getActiveGoal } from "../api/goals";
import { getWeights } from "../api/weight";
import { getCurrentInsight } from "../api/insights";
import { explainApiError } from "../shared/api/errors";
import { isSetupRequired } from "../features/setup/isSetupRequired";
import { Link } from "react-router-dom";
import { SetupRequiredCard } from "../features/dashboard/components/SetupRequiredCard";
import { DashboardErrorAlert } from "../features/dashboard/components/DashboardErrorAlert";
import { DashboardLoadingCard } from "../features/dashboard/components/DashboardLoadingCard";


function calcBmi(heightCm: number, weightKg: number) {
  const h = heightCm / 100;
  return weightKg / (h * h);
}


export default function Dashboard() {
  const { loginWithRedirect, logout, isAuthenticated, isLoading, user } = useAuth0();

  const meQ = useAuthedQuery("me", getMe);
  const profileQ = useAuthedQuery("profile", getProfile);
  const goalQ = useAuthedQuery("activeGoal", getActiveGoal);
  const weightsQ = useAuthedQuery("weights", getWeights);
  const insightQ = useAuthedQuery("currentInsight", getCurrentInsight);

  const setupRequired = isSetupRequired(profileQ.error);

  const anyLoading =
    meQ.loading || profileQ.loading || goalQ.loading || weightsQ.loading || insightQ.loading;

  const firstError =
    meQ.error || profileQ.error || goalQ.error || weightsQ.error || insightQ.error;

  const firstErrorMessage = firstError ? explainApiError(firstError) : "";

  const latestWeight = weightsQ.data?.[0]?.weightKg;
  const heightCm = profileQ.data?.heightCm;

  const bmi =
    heightCm && latestWeight ? calcBmi(heightCm, latestWeight) : null;

  return (
    <div className="p-4 space-y-4">
      <PageHeader
        title="Today"
        subtitle={
          isLoading
            ? "Auth loading..."
            : isAuthenticated
              ? `Signed in${user?.email ? `: ${user.email}` : ""}`
              : "Signed out"
        }
        actions={
          !isAuthenticated ? (
            <Button variant="primary" onClick={() => loginWithRedirect()}>
              Log in
            </Button>
          ) : (
            <Button onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}>
              Log out
            </Button>
          )
        }
      />

      {!isAuthenticated && !isLoading && (
        <Card>
          <CardTitle>Welcome</CardTitle>
          <CardSubtitle>Log in to view your numbers and insights.</CardSubtitle>
          <CardBody>
            <Button variant="primary" fullWidth onClick={() => loginWithRedirect()}>
              Continue
            </Button>
          </CardBody>
        </Card>
      )}

      {isAuthenticated && setupRequired && <SetupRequiredCard />}

      {!setupRequired && (
        <>
          {isAuthenticated && anyLoading && <DashboardLoadingCard loadingFlags={{
            me: meQ.loading,
            profile: profileQ.loading,
            goal: goalQ.loading,
            weights: weightsQ.loading,
            insight: insightQ.loading
          }} />}

          {isAuthenticated && firstError && <DashboardErrorAlert message={firstErrorMessage} />}

          {isAuthenticated && !firstError && (
            <>
              <Card>
                <CardTitle>Profile</CardTitle>
                <CardSubtitle>Identity + basics</CardSubtitle>
                <CardBody>
                  <div className="text-sm space-y-1">
                    <div><span className="text-gray-600">sub:</span> {meQ.data?.sub ?? "-"}</div>
                    <div><span className="text-gray-600">email:</span> {meQ.data?.email ?? user?.email ?? "-"}</div>
                    <div><span className="text-gray-600">height:</span> {heightCm ? `${heightCm} cm` : "-"}</div>
                  </div>
                </CardBody>
              </Card>

              <Card>
                <CardTitle>Numbers don’t lie</CardTitle>
                <CardSubtitle>Latest stats</CardSubtitle>
                <CardBody>
                  <div className="grid grid-cols-2 gap-3 text-sm">
                    <div className="rounded-lg border p-3">
                      <div className="text-gray-600">Latest weight</div>
                      <div className="text-lg font-semibold">
                        {latestWeight ? `${latestWeight.toFixed(1)} kg` : "-"}
                      </div>
                    </div>

                    <div className="rounded-lg border p-3">
                      <div className="text-gray-600">BMI</div>
                      <div className="text-lg font-semibold">
                        {bmi ? bmi.toFixed(1) : "-"}
                      </div>
                    </div>
                  </div>
                </CardBody>
              </Card>

              <Card>
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <CardTitle>AI insight</CardTitle>
                    <CardSubtitle>Personal recommendations</CardSubtitle>
                  </div>
                  <span className="rounded-full border px-2 py-1 text-xs text-gray-700">
                    {insightQ.data?.source ?? "—"}
                  </span>
                </div>

                <CardBody>
                  {insightQ.data?.payload ? (
                    <div className="text-sm space-y-3">
                      <ul className="list-disc pl-5 space-y-1">
                        {insightQ.data.payload.recommendations.map((r, i) => (
                          <li key={i}>{r}</li>
                        ))}
                      </ul>

                      <div>
                        <div className="text-gray-600">Reflection question</div>
                        <div className="font-medium">{insightQ.data.payload.reflection_question}</div>
                      </div>

                      <div>
                        <div className="text-gray-600">Summary</div>
                        <div>{insightQ.data.payload.summary}</div>
                      </div>
                    </div>
                  ) : (
                    <div className="text-sm text-gray-600">No insight yet.</div>
                  )}
                </CardBody>
              </Card>
            </>
          )}
        </>
      )}
    </div>
  );
}
