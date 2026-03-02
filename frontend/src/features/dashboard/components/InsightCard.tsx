import { Card, CardBody, CardTitle } from "../../../shared/ui/Card";
import type { Insight } from "../../../shared/types";

interface InsightCardProps {
    insight: Insight | null;
}

export function InsightCard({ insight }: InsightCardProps) {
    if (!insight) {
        return null;
    }

    const { payload, source, createdAt } = insight;
    const sourceLabel =
        source === "openai" ? "AI Generated" :
            source === "cache" || source === "cached" ? "Cached" :
                "Generic Wellness Tip";

    return (
        <Card>
            <div className="flex items-center justify-between">
                <CardTitle>✨ AI Insight</CardTitle>
                <span className="text-xs font-medium text-slate-500 bg-slate-100 px-2 py-1 rounded">
                    {sourceLabel}
                </span>
            </div>
            <CardBody>
                <p className="text-sm text-slate-900 mb-3">{payload.summary}</p>

                {payload.recommendations.length > 0 && (
                    <div className="mb-4">
                        <h4 className="text-xs font-semibold text-slate-700 mb-2">
                            Recommendations
                        </h4>
                        <ul className="space-y-1">
                            {payload.recommendations.map((rec, idx) => (
                                <li key={idx} className="text-xs text-slate-600 flex gap-2">
                                    <span className="text-green-600">→</span>
                                    <span>{rec}</span>
                                </li>
                            ))}
                        </ul>
                    </div>
                )}

                {payload.reflection_question && (
                    <div className="p-2 bg-green-50 rounded border border-green-100">
                        <p className="text-xs italic text-slate-700">
                            💭 <span className="font-medium">Reflect:</span>{" "}
                            {payload.reflection_question}
                        </p>
                    </div>
                )}

                <div className="text-xs text-slate-500 mt-3">
                    {new Date(createdAt).toLocaleDateString()}
                </div>
            </CardBody>
        </Card>
    );
}
