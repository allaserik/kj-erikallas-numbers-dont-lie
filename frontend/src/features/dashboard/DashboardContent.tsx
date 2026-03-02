import { Alert } from "../../shared/ui/Alert";
import { Card, CardBody } from "../../shared/ui/Card";
import { Spinner } from "../../shared/ui/Spinner";
import { BMICard } from "./components/BMICard";
import { WellnessScoreCard } from "./components/WellnessScoreCard";
import { GoalCard } from "./components/GoalCard";
import { InsightCard } from "./components/InsightCard";
import { QuickNav } from "./components/QuickNav";
import type { DashboardState } from "./useDashboardData";

interface DashboardContentProps {
    isAuthenticated: boolean;
    data: DashboardState;
}

export function DashboardContent({ isAuthenticated, data }: DashboardContentProps) {
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

            {data.isLoading && (
                <Card>
                    <CardBody>
                        <Spinner label="Loading your dashboard..." />
                    </CardBody>
                </Card>
            )}

            {data.error && (
                <Alert
                    tone="error"
                    title="Error Loading Dashboard"
                    message={data.error.message || "Failed to load dashboard data"}
                />
            )}

            {isAuthenticated && !data.isLoading && !data.error && !data.profile && (
                <Card>
                    <CardBody>
                        <div className="space-y-3">
                            <h3 className="text-lg font-semibold text-slate-900">Complete Your Profile</h3>
                            <p className="text-sm text-slate-600">
                                Please fill in your health profile to get started with tracking your wellness.
                            </p>
                            <a
                                href="/profile"
                                className="inline-block px-4 py-2 bg-blue-600 text-white rounded font-medium hover:bg-blue-700 transition"
                            >
                                Go to Profile →
                            </a>
                        </div>
                    </CardBody>
                </Card>
            )}

            {isAuthenticated && !data.isLoading && !data.error && data.profile && (
                <>
                    <BMICard summary={data.summary} isLoading={data.isLoading} />
                    <WellnessScoreCard profile={data.profile} isLoading={data.isLoading} />
                    <GoalCard activeGoal={data.activeGoal} />
                    <InsightCard insight={data.insight} />
                    <QuickNav />
                </>
            )}
        </div>
    );
}
