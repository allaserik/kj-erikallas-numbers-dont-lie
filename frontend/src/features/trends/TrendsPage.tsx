import { Alert } from "../../shared/ui/Alert";
import { Spinner } from "../../shared/ui/Spinner";
import { useWeightChartData } from "./useWeightChartData";
import { WeightChart } from "./components/WeightChart";

export default function TrendsPage() {
    const chartData = useWeightChartData();

    return (
        <div className="space-y-4 pb-32 md:pb-4">
            <div>
                <h1 className="text-2xl font-bold text-slate-900">Trends</h1>
                <p className="text-slate-600">Track your progress over time</p>
            </div>

            {chartData.isLoading && (
                <div className="flex justify-center py-12">
                    <Spinner />
                </div>
            )}

            {chartData.error && (
                <Alert
                    tone="error"
                    title="Error Loading Trends"
                    message={chartData.error.message || "Failed to load weight data"}
                />
            )}

            {!chartData.isLoading && !chartData.error && (
                <>
                    <WeightChart
                        points={chartData.points}
                        targetWeight={chartData.targetWeight}
                        minWeight={chartData.minWeight}
                        maxWeight={chartData.maxWeight}
                    />

                    {chartData.points.length === 0 && (
                        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                            <p className="text-blue-900">
                                Start recording your weight to see your progress over time. Visit the
                                <a href="/checkin" className="font-medium underline">
                                    {" "}
                                    Check In
                                </a>{" "}
                                page to add your first entry.
                            </p>
                        </div>
                    )}
                </>
            )}
        </div>
    );
}
