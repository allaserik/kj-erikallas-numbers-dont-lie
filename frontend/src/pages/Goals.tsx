import { useAuthedQuery } from "../shared/auth/useAuthedQuery";
import { getActiveGoal, getGoalProgress } from "../api/goals";
import { Card, CardBody, CardTitle } from "../shared/ui/Card";
import { Alert } from "../shared/ui/Alert";
import { Spinner } from "../shared/ui/Spinner";
import { explainApiError } from "../shared/api/errors";

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

          {/* Progress Bar */}
          <div className="mb-6">
            <div className="flex justify-between items-center mb-2">
              <span className="text-sm font-semibold">Progress</span>
              <span className="text-sm font-bold text-blue-600">{progressPercent}%</span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-3">
              <div
                className={`h-3 rounded-full transition-all ${isOnTrack ? "bg-green-500" : "bg-orange-500"
                  }`}
                style={{ width: `${Math.min(progressPercent, 100)}%` }}
              />
            </div>
            <div className="text-xs text-gray-500 mt-1">
              {isOnTrack ? "✓ On track" : "⚠ Behind schedule"}
            </div>
          </div>

          {/* Goal Details */}
          <div className="space-y-3 border-t pt-4">
            {goal.targetWeightKg && (
              <div className="flex justify-between">
                <span className="text-gray-600 text-sm">Target Weight:</span>
                <span className="font-semibold">{goal.targetWeightKg} kg</span>
              </div>
            )}

            {goal.targetActivityDaysPerWeek && (
              <div className="flex justify-between">
                <span className="text-gray-600 text-sm">Target Activity:</span>
                <span className="font-semibold">{goal.targetActivityDaysPerWeek} days/week</span>
              </div>
            )}

            {progress?.days_remaining !== undefined && (
              <div className="flex justify-between">
                <span className="text-gray-600 text-sm">Days Remaining:</span>
                <span className={`font-semibold ${progress.days_remaining < 0 ? 'text-red-600' : ''}`}>
                  {Math.max(0, progress.days_remaining)} days
                </span>
              </div>
            )}

            {progress?.current_value !== undefined && (
              <div className="flex justify-between">
                <span className="text-gray-600 text-sm">Current Value:</span>
                <span className="font-semibold">{progress.current_value}</span>
              </div>
            )}
          </div>

          {/* Milestones */}
          {progress?.milestone_details && progress.milestone_details.length > 0 && (
            <div className="mt-6 pt-4 border-t">
              <h3 className="text-sm font-semibold mb-3">Milestones Achieved</h3>
              <div className="space-y-2">
                {progress.milestone_details.map((m, idx) => (
                  <div key={idx} className="flex items-center space-x-2">
                    <span className="text-green-600">✓</span>
                    <span className="text-sm">{m.percentage}% Complete</span>
                    <span className="text-xs text-gray-500">
                      {new Date(m.completed_at).toLocaleDateString()}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </CardBody>
      </Card>

      {/* Goal Metadata */}
      <Card>
        <CardBody>
          <CardTitle>Goal Info</CardTitle>
          <div className="space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-600">Started:</span>
              <span>{new Date(goal.createdAt).toLocaleDateString()}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Last Updated:</span>
              <span>{new Date(goal.updatedAt).toLocaleDateString()}</span>
            </div>
          </div>
        </CardBody>
      </Card>

      {progressQ.error && (
        <Alert title="Error loading progress" message={explainApiError(progressQ.error)} tone="error" />
      )}
    </div>
  );
}
