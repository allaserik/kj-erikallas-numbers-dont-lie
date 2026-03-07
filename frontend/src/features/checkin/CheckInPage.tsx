import { useState } from "react";
import { useAppAuth } from "../../shared/auth/AuthContext";
import { useAuthToken } from "../../shared/auth/useAuthToken";
import { recordWeight } from "../../shared/api/weight";
import { Button } from "../../shared/ui/Button";
import { Card, CardBody, CardTitle, CardSubtitle } from "../../shared/ui/Card";
import { TextField } from "../../shared/ui/TextField";
import { Alert } from "../../shared/ui/Alert";
import { ApiError } from "../../shared/api/client";

type ValidationErrors = {
    weight?: string;
};

function validateForm(weight: string): ValidationErrors {
    const errors: ValidationErrors = {};
    const w = Number(weight);

    if (!weight || w <= 0) errors.weight = "Weight is required and must be positive";
    if (w > 300) errors.weight = "Weight seems too high, please check";

    return errors;
}

// CheckInPage: Quick weight check-in
export default function CheckInPage() {
    const { isAuthenticated } = useAppAuth();
    const getToken = useAuthToken();
    const [weight, setWeight] = useState("");
    const [date, setDate] = useState(new Date().toISOString().split("T")[0]); // Today's date
    const [notes, setNotes] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);
    const [validationErrors, setValidationErrors] = useState<ValidationErrors>({});

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setSuccess(false);

        const errors = validateForm(weight);
        if (Object.keys(errors).length > 0) {
            setValidationErrors(errors);
            return;
        }

        setIsSubmitting(true);
        try {
            const token = await getToken();
            if (!token) throw new Error("Not authenticated");

            await recordWeight(
                {
                    weight: Number(weight),
                    date,
                    notes: notes || undefined,
                },
                token
            );

            // Reset form
            setWeight("");
            setDate(new Date().toISOString().split("T")[0]);
            setNotes("");
            setSuccess(true);
            setValidationErrors({});

            // Hide success message after 3 seconds
            setTimeout(() => setSuccess(false), 3000);
        } catch (err) {
            const message = err instanceof ApiError ? err.message : "Failed to record weight";
            setError(message);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="space-y-4 pb-32 md:pb-4">
            <h1 className="text-2xl font-bold text-slate-900">Check In</h1>
            <p className="text-slate-600">Record your weight and track your progress.</p>

            {!isAuthenticated && (
                <Alert
                    tone="info"
                    title="Not Authenticated"
                    message="Please log in to record your weight."
                />
            )}

            <Card>
                <CardTitle>Record Weight</CardTitle>
                <CardSubtitle>Quick weight entry for today or another date</CardSubtitle>
                <CardBody>
                    {error && <Alert tone="error" title="Error" message={error} />}
                    {success && (
                        <Alert tone="success" title="Success" message="Weight recorded successfully!" />
                    )}

                    <form onSubmit={handleSubmit} className="space-y-4">
                        <TextField
                            label="Weight (kg)"
                            type="number"
                            value={weight}
                            onChange={(e) => {
                                setWeight(e.target.value);
                                if (validationErrors.weight) {
                                    setValidationErrors({});
                                }
                            }}
                            error={validationErrors.weight}
                            min="20"
                            max="300"
                            step="0.1"
                            placeholder="75.5"
                            disabled={isSubmitting || !isAuthenticated}
                            required
                        />

                        <TextField
                            label="Date"
                            type="date"
                            value={date}
                            onChange={(e) => setDate(e.target.value)}
                            disabled={isSubmitting || !isAuthenticated}
                            required
                        />

                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-1">
                                Notes (optional)
                            </label>
                            <textarea
                                value={notes}
                                onChange={(e) => setNotes(e.target.value)}
                                placeholder="How are you feeling? Any comments about this check-in?"
                                disabled={isSubmitting || !isAuthenticated}
                                className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-green-200 transition"
                                rows={3}
                            />
                        </div>

                        <Button
                            type="submit"
                            fullWidth
                            disabled={isSubmitting || !isAuthenticated}
                        >
                            {isSubmitting ? "Recording..." : "Record Weight"}
                        </Button>
                    </form>
                </CardBody>
            </Card>

            {/* Quick Tips Card */}
            <Card>
                <CardTitle>Tips for Accurate Tracking</CardTitle>
                <CardBody>
                    <ul className="space-y-2 text-sm text-slate-600">
                        <li className="flex gap-2">
                            <span className="text-green-600 font-bold">•</span>
                            <span>Weigh yourself at the same time each day (morning is best)</span>
                        </li>
                        <li className="flex gap-2">
                            <span className="text-green-600 font-bold">•</span>
                            <span>Use the same scale for consistency</span>
                        </li>
                        <li className="flex gap-2">
                            <span className="text-green-600 font-bold">•</span>
                            <span>Weight naturally fluctuates—focus on weekly trends</span>
                        </li>
                        <li className="flex gap-2">
                            <span className="text-green-600 font-bold">•</span>
                            <span>Add notes to track how you felt that day</span>
                        </li>
                    </ul>
                </CardBody>
            </Card>
        </div>
    );
}
