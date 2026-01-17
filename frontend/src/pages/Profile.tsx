import { useEffect, useState } from "react";
import { useAuth0 } from "@auth0/auth0-react";

import { Button } from "../shared/ui/Button";
import { Card, CardBody, CardSubtitle, CardTitle } from "../shared/ui/Card";
import { TextField } from "../shared/ui/TextField";
import { SelectField } from "../shared/ui/SelectField";
import { Alert } from "../shared/ui/Alert";
import { Spinner } from "../shared/ui/Spinner";

import { useAuthToken } from "../shared/auth/useAuthToken";
import { explainApiError, fieldErrorsByName } from "../shared/api/errors";
import { getProfile, upsertProfile, type UpsertProfileRequest } from "../api/profile";

type FormState = {
    birthYear: string; // keep as string in UI, convert on submit
    gender: string;
    heightCm: string;
    baselineActivityLevel: UpsertProfileRequest["baselineActivityLevel"];
};

const activityOptions = [
    { value: "sedentary", label: "Sedentary" },
    { value: "light", label: "Light" },
    { value: "moderate", label: "Moderate" },
    { value: "active", label: "Active" },
    { value: "very_active", label: "Very active" },
] as const;

export default function ProfilePage() {
    const { isAuthenticated, isLoading: authLoading, loginWithRedirect } = useAuth0();
    const getToken = useAuthToken();

    const [loading, setLoading] = useState(false);
    const [loadingProfile, setLoadingProfile] = useState(false);

    const [submitError, setSubmitError] = useState("");
    const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

    const [form, setForm] = useState<FormState>({
        birthYear: "",
        gender: "",
        heightCm: "",
        baselineActivityLevel: "moderate",
    });

    // Load existing profile once authenticated
    useEffect(() => {
        let alive = true;

        (async () => {
            setSubmitError("");
            setFieldErrors({});

            if (!isAuthenticated) return;

            setLoadingProfile(true);
            try {
                const token = await getToken();
                if (!token) return;

                const p = await getProfile(token);
                if (!alive) return;

                setForm({
                    birthYear: p.birthYear ? String(p.birthYear) : "",
                    gender: p.gender ?? "",
                    heightCm: String(p.heightCm ?? ""),
                    baselineActivityLevel: p.baselineActivityLevel ?? "moderate",
                });
            } catch (e) {
                if (!alive) return;
                setSubmitError(explainApiError(e));
            } finally {
                if (alive) setLoadingProfile(false);
            }
        })();

        return () => {
            alive = false;
        };
    }, [isAuthenticated, getToken]);

    async function onSubmit() {
        setSubmitError("");
        setFieldErrors({});

        if (!isAuthenticated) return;

        setLoading(true);
        try {
            const token = await getToken();
            if (!token) throw new Error("Not authenticated");

            const payload: UpsertProfileRequest = {
                birthYear: form.birthYear ? Number(form.birthYear) : null,
                gender: form.gender ? form.gender : null,
                heightCm: Number(form.heightCm), // backend validates min 50
                baselineActivityLevel: form.baselineActivityLevel,
            };

            await upsertProfile(token, payload);

            // Optional: show small success state later. For now: no error means success.
        } catch (e) {
            // If backend sends fieldErrors[] on 400, map them to inputs
            const fe = fieldErrorsByName(e);
            if (Object.keys(fe).length > 0) setFieldErrors(fe);

            // Always show a top-level message as well (helps when error is non-field related)
            setSubmitError(explainApiError(e));
        } finally {
            setLoading(false);
        }
    }

    if (authLoading) {
        return (
            <div className="p-4">
                <Spinner label="Auth loading..." />
            </div>
        );
    }

    if (!isAuthenticated) {
        return (
            <div className="p-4 space-y-4">
                <Card>
                    <CardTitle>Profile setup</CardTitle>
                    <CardSubtitle>You need to log in to edit your profile.</CardSubtitle>
                    <CardBody>
                        <Button variant="primary" fullWidth onClick={() => loginWithRedirect()}>
                            Log in
                        </Button>
                    </CardBody>
                </Card>
            </div>
        );
    }

    return (
        <div className="p-4 space-y-4">
            <Card>
                <CardTitle>Profile</CardTitle>
                <CardSubtitle>Used for BMI and better recommendations.</CardSubtitle>

                <CardBody>
                    {loadingProfile && <Spinner label="Loading profile..." />}

                    {submitError && (
                        <div className="mt-3">
                            <Alert title="Error" message={submitError} tone="warning" />
                        </div>
                    )}

                    <div className="mt-4 space-y-4">
                        <TextField
                            label="Height (cm)"
                            inputMode="numeric"
                            value={form.heightCm}
                            onChange={(e) => setForm((f) => ({ ...f, heightCm: e.target.value }))}
                            placeholder="e.g. 182"
                            error={fieldErrors["heightCm"]}
                        />

                        <TextField
                            label="Birth year (optional)"
                            inputMode="numeric"
                            value={form.birthYear}
                            onChange={(e) => setForm((f) => ({ ...f, birthYear: e.target.value }))}
                            placeholder="e.g. 1984"
                            error={fieldErrors["birthYear"]}
                        />

                        <TextField
                            label="Gender (optional)"
                            value={form.gender}
                            onChange={(e) => setForm((f) => ({ ...f, gender: e.target.value }))}
                            placeholder="e.g. male / female / other"
                            error={fieldErrors["gender"]}
                        />

                        <SelectField
                            label="Baseline activity level"
                            value={form.baselineActivityLevel}
                            onChange={(e) =>
                                setForm((f) => ({
                                    ...f,
                                    baselineActivityLevel: e.target.value as FormState["baselineActivityLevel"],
                                }))
                            }
                            options={activityOptions as unknown as { value: string; label: string }[]}
                            error={fieldErrors["baselineActivityLevel"]}
                        />

                        <Button variant="primary" fullWidth disabled={loading} onClick={onSubmit}>
                            {loading ? "Saving..." : "Save profile"}
                        </Button>
                    </div>
                </CardBody>
            </Card>
        </div>
    );
}
