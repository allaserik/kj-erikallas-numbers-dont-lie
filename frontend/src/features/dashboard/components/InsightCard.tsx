import { Card, CardBody, CardTitle } from "../../../shared/ui/Card";
import type { Insight } from "../../../shared/types";

interface InsightCardProps {
    insight: Insight | null;
}

export function InsightCard({ insight }: InsightCardProps) {
    if (!insight) {
        return null;
    }

    return (
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
    );
}
