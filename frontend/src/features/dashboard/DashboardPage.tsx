import { useAuth0 } from "@auth0/auth0-react";
import { useAuthedQuery } from "../../shared/auth/useAuthedQuery";
import { getLatestWeight } from "../../shared/api/weight";
import { getLatestInsight } from "../../shared/api/insights";
import { getDashboardSummary } from "../../shared/api/dashboard";
import { Button } from "../../shared/ui/Button";
import { Card, CardBody, CardTitle, CardSubtitle } from "../../shared/ui/Card";
import { Alert } from "../../shared/ui/Alert";
import { Spinner } from "../../shared/ui/Spinner";
import { ArrowUpIcon, ArrowDownIcon, EllipsisHorizontalIcon } from "@heroicons/react/24/outline";

// Utility to format weight change
function formatWeightChange(change: number): { icon: React.ReactNode; color: string; text: string } {
    if (change > 0) {
        return {
            icon: <ArrowUpIcon className="h-4 w-4" />,
            color: "text-red-600",
            text: `+${change.toFixed(1)} kg`,
        };
    } else if (change < 0) {
        return {
            icon: <ArrowDownIcon className="h-4 w-4" />,
            color: "text-green-600",
            text: `${change.toFixed(1)} kg`,
        };
    }
    return {
        icon: <EllipsisHorizontalIcon className="h-4 w-4" />,
        color: "text-slate-600",
        text: "No change",
    };
}

// DashboardPage: Health summary and quick actions
export default function DashboardPage() {
    const { isAuthenticated } = useAuth0();

    // Try to load dashboard summary (simplified aggregated endpoint)
    const summaryQ = useAuthedQuery("dashboardSummary", getDashboardSummary, isAuthenticated);

    // Fallback individual queries if summary fails
    const latestWeightQ = useAuthedQuery(
        "latestWeight",
        getLatestWeight,
        isAuthenticated && !summaryQ.data
    );
    const latestInsightQ = useAuthedQuery(
        "latestInsight",
        getLatestInsight,
        isAuthenticated && !summaryQ.data
    );

    const isLoading = summaryQ.loading || latestWeightQ.loading;
    const hasError = summaryQ.error || latestWeightQ.error;

    // Use summary data if available, otherwise use individual queries
    const latestWeight = summaryQ.data?.currentWeight || latestWeightQ.data;
    const activeGoal = summaryQ.data?.activeGoal || null;
    const insight = summaryQ.data?.recentInsight || latestInsightQ.data;
    const weightTrend = summaryQ.data?.weightTrend;

    // Calculate weight change since 7 days ago
    const hasWeightTrend = latestWeight && weightTrend;

    return (
        <div className="space-y-4 pb-32 md:pb-4">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold text-slate-900">Dashboard</h1>
                    <p className="text-slate-600">Your wellness at a glance</p>
                </div>
            </div>

            {!isAuthenticated && (
                <Alert
                    tone="info"
                    title="Not Authenticated"
                    message="Please log in to view your dashboard."
                />
            )}

            {isLoading && (
                <Card>
                    <CardBody>
                        <Spinner label="Loading your dashboard..." />
                    </CardBody>
                </Card>
            )}

            {hasError && (
                <Alert
                    tone="error"
                    title="Error Loading Dashboard"
                    message={
                        summaryQ.error?.message ||
                        latestWeightQ.error?.message ||
                        "Failed to load dashboard data"
                    }
                />
            )}

            {/* Current Weight Card */}
            {latestWeight && (
                <Card>
                    <CardTitle>Current Weight</CardTitle>
                    <CardBody>
                        <div className="flex items-end gap-4">
                            <div>
                                <div className="text-4xl font-bold text-slate-900">
                                    {latestWeight.weight}
                                </div>
                                <div className="text-sm text-slate-600">kg</div>
                            </div>
                            {hasWeightTrend && (
                                <div className={`flex items-center gap-1 ${formatWeightChange(weightTrend!.change).color}`}>
                                    {formatWeightChange(weightTrend!.change).icon}
                                    <span className="text-sm font-medium">
                                        {formatWeightChange(weightTrend!.change).text}
                                    </span>
                                </div>
                            )}
                        </div>
                        <div className="mt-2 text-xs text-slate-500">
                            Last recorded: {new Date(latestWeight.date).toLocaleDateString()}
                        </div>
                        <a href="/checkin">
                            <Button fullWidth className="mt-3">
                                Record New Weight
                            </Button>
                        </a>
                    </CardBody>
                </Card>
            )}

            {!latestWeight && isAuthenticated && !isLoading && (
                <Card>
                    <CardBody>
                        <p className="text-slate-600 mb-4">
                            Start tracking by recording your first weight entry.
                        </p>
                        <a href="/checkin">
                            <Button fullWidth>Record Your Weight</Button>
                        </a>
                    </CardBody>
                </Card>
            )}

            {/* Active Goal Card */}
            {activeGoal && (
                <Card>
                    <CardTitle>{activeGoal.title}</CardTitle>
                    <CardSubtitle>{activeGoal.description}</CardSubtitle>
                    <CardBody>
                        <div className="space-y-2">
                            {activeGoal.progress !== undefined && (
                                <div>
                                    <div className="flex justify-between items-center mb-1">
                                        <span className="text-sm text-slate-600">Progress</span>
                                        <span className="text-sm font-medium text-slate-900">
                                            {activeGoal.progress}%
                                        </span>
                                    </div>
                                    <div className="w-full bg-slate-200 rounded-full h-2">
                                        <div
                                            className="bg-green-600 h-2 rounded-full transition-all"
                                            style={{ width: `${activeGoal.progress}%` }}
                                        />
                                    </div>
                                </div>
                            )}
                            <div className="text-xs text-slate-500">
                                Target date: {new Date(activeGoal.targetDate).toLocaleDateString()}
                            </div>
                        </div>
                    </CardBody>
                </Card>
            )}

            {/* AI Insight Card */}
            {insight && (
                <Card>
                    <div className="flex items-center gap-2">
                        <CardTitle>✨ AI Insight</CardTitle>
                    </div>
                    <CardBody>
                        <p className="text-sm text-slate-900 mb-3">{insight.summary}</p>

                        {insight.recommendations.length > 0 && (
                            <div className="mb-4">
                                <h4 className="text-xs font-semibold text-slate-700 mb-2">
                                    Recommendations
                                </h4>
                                <ul className="space-y-1">
                                    {insight.recommendations.map((rec, idx) => (
                                        <li key={idx} className="text-xs text-slate-600 flex gap-2">
                                            <span className="text-green-600">→</span>
                                            <span>{rec}</span>
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        )}

                        {insight.reflectionQuestion && (
                            <div className="p-2 bg-green-50 rounded border border-green-100">
                                <p className="text-xs italic text-slate-700">
                                    💭 <span className="font-medium">Reflect:</span>{" "}
                                    {insight.reflectionQuestion}
                                </p>
                            </div>
                        )}

                        <div className="text-xs text-slate-500 mt-3">
                            Generated: {new Date(insight.generatedAt).toLocaleDateString()}
                        </div>
                    </CardBody>
                </Card>
            )}

            {/* Quick Navigation */}
            <div className="grid grid-cols-2 gap-3">
                <a href="/profile">
                    <Button fullWidth className="text-center">
                        👤 Profile
                    </Button>
                </a>
                <a href="/goals">
                    <Button fullWidth className="text-center">
                        🎯 Goals
                    </Button>
                </a>
                <a href="/trends">
                    <Button fullWidth className="text-center">
                        📈 Trends
                    </Button>
                </a>
                <a href="/checkin">
                    <Button fullWidth className="text-center">
                        ✅ Check In
                    </Button>
                </a>
            </div>
        </div>
    );
}
