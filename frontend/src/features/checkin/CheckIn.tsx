import { useState } from "react";
import { useAuthedQuery } from "../../shared/auth/useAuthedQuery";
import { addWeight, getWeights } from "../../api/weight";
import { getProfile } from "../../api/profile";
import { Button } from "../../shared/ui/Button";
import { TextField } from "../../shared/ui/TextField";
import { Card, CardBody, CardTitle } from "../../shared/ui/Card";
import { Alert } from "../../shared/ui/Alert";
import { Spinner } from "../../shared/ui/Spinner";
import { explainApiError } from "../../shared/api/errors";

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
                {/* ...existing code... */}
            </Card>
        </div>
    );
}
// ...existing code from pages/CheckIn.tsx will be moved here
