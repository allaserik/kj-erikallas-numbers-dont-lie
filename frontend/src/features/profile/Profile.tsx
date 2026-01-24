import { useEffect, useState } from "react";
import { useAuth0 } from "@auth0/auth0-react";
import { Button } from "../../shared/ui/Button";
import { Card, CardBody, CardSubtitle, CardTitle } from "../../shared/ui/Card";
import { TextField } from "../../shared/ui/TextField";
import { SelectField } from "../../shared/ui/SelectField";
import { Alert } from "../../shared/ui/Alert";
import { Spinner } from "../../shared/ui/Spinner";
import { useAuthToken } from "../../shared/auth/useAuthToken";
import { explainApiError, fieldErrorsByName } from "../../shared/api/errors";
import { getProfile, upsertProfile, type UpsertProfileRequest } from "../../api/profile";

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
// ...existing code...
// ...existing code from pages/Profile.tsx will be moved here
