import { useAuthedQuery } from "../shared/auth/useAuthedQuery";
import { getWeights } from "../api/weight";
import { getProfile } from "../api/profile";
import { Card, CardBody, CardTitle } from "../shared/ui/Card";
import { Alert } from "../shared/ui/Alert";
import { Spinner } from "../shared/ui/Spinner";
import { explainApiError } from "../shared/api/errors";

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

              {/* Weight Changes */}
              <div className="grid grid-cols-3 gap-3">
                {delta7d !== null && (
                  <div className={`p-3 rounded ${delta7d < 0 ? 'bg-green-100' : 'bg-red-100'}`}>
                    <div className="text-xs text-gray-600">7 days</div>
                    <div className={`text-lg font-bold ${delta7d < 0 ? 'text-green-700' : 'text-red-700'}`}>
                      {delta7d >= 0 ? '+' : ''}{delta7d.toFixed(1)} kg
                    </div>
                  </div>
                )}

                {delta30d !== null && (
                  <div className={`p-3 rounded ${delta30d < 0 ? 'bg-green-100' : 'bg-red-100'}`}>
                    <div className="text-xs text-gray-600">30 days</div>
                    <div className={`text-lg font-bold ${delta30d < 0 ? 'text-green-700' : 'text-red-700'}`}>
                      {delta30d >= 0 ? '+' : ''}{delta30d.toFixed(1)} kg
                    </div>
                  </div>
                )}

                {delta90d !== null && (
                  <div className={`p-3 rounded ${delta90d < 0 ? 'bg-green-100' : 'bg-red-100'}`}>
                    <div className="text-xs text-gray-600">90 days</div>
                    <div className={`text-lg font-bold ${delta90d < 0 ? 'text-green-700' : 'text-red-700'}`}>
                      {delta90d >= 0 ? '+' : ''}{delta90d.toFixed(1)} kg
                    </div>
                  </div>
                )}
              </div>

              {/* Statistics */}
              {avgWeight && (
                <div className="pt-3 border-t">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Average weight:</span>
                    <span className="font-semibold">{avgWeight} kg</span>
                  </div>
                </div>
              )}

              {latestWeight && heightCm && (
                <div className="flex justify-between text-sm">
                  <span className="text-gray-600">Current BMI:</span>
                  <span className="font-semibold">
                    {(latestWeight / ((heightCm / 100) ** 2)).toFixed(1)}
                  </span>
                </div>
              )}
            </div>
          )}

          {weightsQ.data && weightsQ.data.length === 0 && (
            <Alert title="No data yet" message="Start logging your weight to see trends" tone="info" />
          )}
        </CardBody>
      </Card>

      {/* Weight History Table */}
      {weightsQ.data && weightsQ.data.length > 0 && (
        <Card>
          <CardBody>
            <CardTitle>History</CardTitle>
            <div className="space-y-2 max-h-96 overflow-y-auto">
              {weightsQ.data.slice(0, 20).map((entry, idx) => (
                <div key={idx} className="flex justify-between text-sm py-2 border-b last:border-b-0">
                  <span className="text-gray-600">
                    {new Date(entry.measuredAt).toLocaleDateString()}
                  </span>
                  <span className="font-semibold">{entry.weightKg} kg</span>
                </div>
              ))}
            </div>
          </CardBody>
        </Card>
      )}
    </div>
  );
}
