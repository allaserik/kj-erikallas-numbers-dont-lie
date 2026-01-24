import { useAuthedQuery } from "../../shared/auth/useAuthedQuery";
import { getWeights } from "../../api/weight";
import { getProfile } from "../../api/profile";
import { Card, CardBody, CardTitle } from "../../shared/ui/Card";
import { Alert } from "../../shared/ui/Alert";
import { Spinner } from "../../shared/ui/Spinner";
import { explainApiError } from "../../shared/api/errors";

export default function Trends() {
    const profileQ = useAuthedQuery("profile", getProfile);
    const weightsQ = useAuthedQuery("weights", getWeights);

    const calculateDelta = (entries: typeof weightsQ.data, days: number) => {
        if (!entries || entries.length < 2) return null;

        const now = new Date();
        const cutoff = new Date(now.getTime() - days * 24 * 60 * 60 * 1000);

        const latest = entries[0];
        const oldest = entries.find(e => new Date(e.measuredAt) < cutoff);

        if (!oldest) return null;
        return latest.weightKg - oldest.weightKg;
    };

    const delta7d = calculateDelta(weightsQ.data, 7);
    const delta30d = calculateDelta(weightsQ.data, 30);
    const delta90d = calculateDelta(weightsQ.data, 90);

    const avgWeight = weightsQ.data
        ? (weightsQ.data.reduce((sum, e) => sum + e.weightKg, 0) / weightsQ.data.length).toFixed(1)
        : null;

    const heightCm = profileQ.data?.heightCm;
    const latestWeight = weightsQ.data?.[0]?.weightKg;

    return (
        <div className="max-w-md mx-auto p-4 space-y-4 pb-24">
            <Card>
                <CardBody>
                    <CardTitle>Weight Trends</CardTitle>
                    <p className="text-sm text-gray-600 mb-4">
                        {weightsQ.data?.length || 0} measurements recorded
                    </p>

                    {weightsQ.loading && <Spinner />}
                    {weightsQ.error && (
                        <Alert title="Error loading trends" message={explainApiError(weightsQ.error)} tone="error" />
                    )}

                    {weightsQ.data && weightsQ.data.length > 0 && (
                        <div className="space-y-4">
                            {/* Latest Weight */}
                            <div className="border-l-4 border-blue-500 pl-3">
                                <div className="text-xs text-gray-500">Latest</div>
                                <div className="text-2xl font-bold">{latestWeight} kg</div>
                                <div className="text-xs text-gray-500">
                                    {new Date(weightsQ.data[0].measuredAt).toLocaleDateString()}
                                </div>
                            </div>
                            {/* ...existing code... */}
                        </div>
                    )}
                </CardBody>
            </Card>
        </div>
    );
}
// ...existing code from pages/Trends.tsx will be moved here
