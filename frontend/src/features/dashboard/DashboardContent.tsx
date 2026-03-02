import { Alert } from "../../shared/ui/Alert";
import { Card, CardBody } from "../../shared/ui/Card";
import { Spinner } from "../../shared/ui/Spinner";
import { WeightCard } from "./components/WeightCard";
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

            {isAuthenticated && !data.isLoading && !data.error && (
                <>
                    <WeightCard latestWeight={data.latestWeight} isLoading={data.isLoading} />
                    <GoalCard activeGoal={data.activeGoal} />
                    <InsightCard insight={data.insight} />
                    <QuickNav />
                </>
            )}
        </div>
    );
}
