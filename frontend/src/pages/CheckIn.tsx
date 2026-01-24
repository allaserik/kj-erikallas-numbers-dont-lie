import { useState } from "react";
import { useAuthedQuery } from "../shared/auth/useAuthedQuery";
import { addWeight, getWeights } from "../api/weight";
import { getProfile } from "../api/profile";
import { Button } from "../shared/ui/Button";
import { TextField } from "../shared/ui/TextField";
import { Card, CardBody, CardTitle } from "../shared/ui/Card";
import { Alert } from "../shared/ui/Alert";
import { Spinner } from "../shared/ui/Spinner";
import { explainApiError } from "../shared/api/errors";

export default function CheckIn() {
  const [weight, setWeight] = useState("");
  const [measuredAt, setMeasuredAt] = useState(new Date().toISOString().split("T")[0]);
  const [message, setMessage] = useState<{ type: "success" | "error"; text: string } | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const profileQ = useAuthedQuery("profile", getProfile);
  const weightsQ = useAuthedQuery("weights", getWeights);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setMessage(null);

    if (!weight || isNaN(parseFloat(weight))) {
      setMessage({ type: "error", text: "Please enter a valid weight" });
      return;
    }

    setIsSubmitting(true);
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        throw new Error("No authentication token");
      }

      await addWeight(token, {
        weightKg: parseFloat(weight),
        measuredAt: measuredAt + "T12:00:00+02:00",
      });

      setMessage({ type: "success", text: "Weight logged successfully!" });
      setWeight("");
      setMeasuredAt(new Date().toISOString().split("T")[0]);

      // Refetch weights
      // weightsQ.refetch?.(); // Removed: refetch does not exist on weightsQ
    } catch (err) {
      setMessage({ type: "error", text: explainApiError(err) });
    } finally {
      setIsSubmitting(false);
    }
  };

  const latestWeight = weightsQ.data?.[0]?.weightKg;
  const heightCm = profileQ.data?.heightCm;

  return (
    <div className="max-w-md mx-auto p-4 space-y-4 pb-24">
      <Card>
        <CardBody>
          <CardTitle>Log Weight</CardTitle>
          <p className="text-sm text-gray-600 mb-4">
            Record your weight to track progress towards your goals
          </p>

          <form onSubmit={handleSubmit} className="space-y-4">
            <TextField
              label="Weight (kg)"
              type="number"
              step="0.1"
              value={weight}
              onChange={(e) => setWeight(e.target.value)}
              placeholder="e.g., 75.5"
              disabled={isSubmitting}
            />

            <TextField
              label="Measured Date"
              type="date"
              value={measuredAt}
              onChange={(e) => setMeasuredAt(e.target.value)}
              disabled={isSubmitting}
            />

            {message && (
              <Alert
                title={message.type === "success" ? "Success" : "Error"}
                message={message.text}
                tone={message.type === "success" ? "info" : message.type}
              />
            )}

            <Button
              type="submit"
              disabled={isSubmitting || !weight}
              className="w-full"
            >
              {isSubmitting ? "Saving..." : "Log Weight"}
            </Button>
          </form>
        </CardBody>
      </Card>

      {latestWeight && heightCm && (
        <Card>
          <CardBody>
            <CardTitle>Latest Reading</CardTitle>
            <div className="space-y-2">
              <div className="flex justify-between">
                <span className="text-gray-600">Weight:</span>
                <span className="font-semibold">{latestWeight} kg</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Height:</span>
                <span className="font-semibold">{heightCm} cm</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">BMI:</span>
                <span className="font-semibold">
                  {(latestWeight / ((heightCm / 100) ** 2)).toFixed(1)}
                </span>
              </div>
            </div>
          </CardBody>
        </Card>
      )}

      {weightsQ.loading && <Spinner />}
      {weightsQ.error && (
        <Alert title="Error loading data" message={explainApiError(weightsQ.error)} tone="error" />
      )}
    </div>
  );
}

