import { useAuthedQuery } from "../../shared/auth/useAuthedQuery";
import { getActiveGoal, getGoalProgress } from "../../api/goals";
import { Card, CardBody, CardTitle } from "../../shared/ui/Card";
import { Alert } from "../../shared/ui/Alert";
import { Spinner } from "../../shared/ui/Spinner";
import { explainApiError } from "../../shared/api/errors";

const goalTypeLabels: Record<string, string> = {
    WEIGHT_LOSS: "Lose Weight",
    MAINTAIN_WEIGHT: "Maintain Weight",
    WEIGHT_GAIN: "Gain Weight",
    IMPROVE_FITNESS: "Improve Fitness",
    BUILD_MUSCLE: "Build Muscle",
    ENHANCE_ENDURANCE: "Enhance Endurance",
    IMPROVE_FLEXIBILITY: "Improve Flexibility",
    REDUCE_STRESS: "Reduce Stress",
    BETTER_SLEEP: "Better Sleep",
};

export default function Goals() {
    const goalQ = useAuthedQuery("activeGoal", getActiveGoal);

    const progressQ = useAuthedQuery(
        goalQ.data ? `goalProgress-${goalQ.data.id}` : undefined,
        goalQ.data
            ? () => {
                const token = localStorage.getItem("token");
                if (!token) throw new Error("No token");
                return getGoalProgress(token, goalQ.data!.id);
            }
            : undefined,
        goalQ.data ? undefined : { skip: true }
    );

    if (goalQ.loading) {
        return <Spinner />;
    }

    if (!goalQ.data) {
        return (
            <div className="max-w-md mx-auto p-4 pb-24">
                <Alert title="No Active Goal" message="You don't have an active goal yet. Create one to get started!" tone="info" />
            </div>
        );
    }

    const goal = goalQ.data;
    const progress = progressQ.data;
    const progressPercent = progress?.progress_percentage ?? 0;
    const isOnTrack = progress?.is_on_track ?? false;

    return (
        <div className="max-w-md mx-auto p-4 space-y-4 pb-24">
            <Card>
                <CardBody>
                    <CardTitle>{goalTypeLabels[goal.goalType] || goal.goalType}</CardTitle>

                    {goal.notes && (
                        <p className="text-sm text-gray-600 mb-4">{goal.notes}</p>
                    )}
                    {/* ...existing code... */}
                </CardBody>
            </Card>
        </div>
    );
}
// ...existing code from pages/Goals.tsx will be moved here
