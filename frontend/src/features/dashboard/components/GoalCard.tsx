import { Card, CardBody, CardTitle, CardSubtitle } from "../../../shared/ui/Card";
import type { Goal } from "../../../shared/types";

interface GoalCardProps {
    activeGoal: Goal | null;
}

export function GoalCard({ activeGoal }: GoalCardProps) {
    if (!activeGoal) {
        return null;
    }

    return (
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
    );
}
