import { useState, useCallback, useEffect } from "react";
import { useAuth0 } from "@auth0/auth0-react";
import { useAuthedQuery } from "../../shared/auth/useAuthedQuery";
import { useAuthToken } from "../../shared/auth/useAuthToken";
import { getHealthProfile, upsertHealthProfile } from "../../shared/api/profile";
import { recordWeight, getLatestWeight } from "../../shared/api/weight";
import { ApiError } from "../../shared/api/client";
import type { HealthProfile } from "../../shared/types";

export type FormData = {
    height: number | "";
    weight: number | "";
    age: number | "";
    gender: "MALE" | "FEMALE" | "OTHER";
    activityLevel: "SEDENTARY" | "LIGHTLY_ACTIVE" | "MODERATELY_ACTIVE" | "VERY_ACTIVE" | "EXTREMELY_ACTIVE";
    targetWeight: number | "";
};

export type ValidationErrors = Partial<Record<keyof FormData, string>>;

export interface ProfileState {
    // Data
    profile: HealthProfile | null;
    formData: FormData;
    isEditing: boolean;

    // Loading & Errors
    isLoading: boolean;
    isSaving: boolean;
    loadError: Error | null;
    saveError: string | null;
    saveSuccess: boolean;
    validationErrors: ValidationErrors;

    // Actions
    handleInputChange: (field: keyof FormData, value: string | number) => void;
    handleEdit: () => void;
    handleCancel: () => void;
    handleSave: () => Promise<void>;
    clearSuccessMessage: () => void;
}

function validateForm(data: FormData): ValidationErrors {
    const errors: ValidationErrors = {};

    if (!data.height || data.height <= 0) errors.height = "Height is required and must be positive";
    if (!data.weight || data.weight <= 0) errors.weight = "Weight is required and must be positive";
    if (!data.age || data.age < 13 || data.age > 120) errors.age = "Age must be between 13 and 120";
    if (!data.targetWeight || data.targetWeight <= 0) errors.targetWeight = "Target weight is required and must be positive";

    return errors;
}

function initializeFormData(profile: HealthProfile | null, latestWeight?: number): FormData {
    if (profile) {
        return {
            height: profile.height || "",
            weight: latestWeight || profile.weight || "",
            age: profile.age || "",
            gender: profile.gender || "OTHER",
            activityLevel: profile.activityLevel || "MODERATELY_ACTIVE",
            targetWeight: profile.targetWeight || "",
        };
    }
    return {
        height: "",
        weight: latestWeight || "",
        age: "",
        gender: "OTHER",
        activityLevel: "MODERATELY_ACTIVE",
        targetWeight: "",
    };
}

export function useProfileData(): ProfileState {
    const { isAuthenticated } = useAuth0();
    const getToken = useAuthToken();

    const [isEditing, setIsEditing] = useState(false);
    const [isSaving, setIsSaving] = useState(false);
    const [saveError, setSaveError] = useState<string | null>(null);
    const [saveSuccess, setSaveSuccess] = useState(false);
    const [validationErrors, setValidationErrors] = useState<ValidationErrors>({});

    // Fetch health profile
    const profileQ = useAuthedQuery("healthProfile", getHealthProfile, isAuthenticated && !isEditing);

    // Fetch latest weight entry (shown in profile view)
    const weightQ = useAuthedQuery("latestWeight", getLatestWeight, isAuthenticated && !isEditing);

    // Initialize form as empty - will be populated by useEffect below
    const [formData, setFormData] = useState<FormData>({
        height: "",
        weight: "",
        age: "",
        gender: "OTHER",
        activityLevel: "MODERATELY_ACTIVE",
        targetWeight: "",
    });

    const handleInputChange = useCallback(
        (field: keyof FormData, value: string | number) => {
            setFormData((prev) => ({ ...prev, [field]: value }));
            if (validationErrors[field]) {
                setValidationErrors((prev) => {
                    const next = { ...prev };
                    delete next[field];
                    return next;
                });
            }
        },
        [validationErrors]
    );

    const handleEdit = useCallback(() => {
        setIsEditing(true);
        if (profileQ.data) {
            setFormData(initializeFormData(profileQ.data, weightQ.data?.weight));
        }
    }, [profileQ.data, weightQ.data]);

    const handleCancel = useCallback(() => {
        setIsEditing(false);
        setSaveError(null);
        setValidationErrors({});
    }, []);

    // Update form data when profile or weight data loads
    useEffect(() => {
        if (!isEditing) {
            setFormData(
                initializeFormData(profileQ.data || null, weightQ.data?.weight)
            );
        }
    }, [profileQ.data, weightQ.data, isEditing]);

    // After successfully saving, fetch and preserve the latest weight
    useEffect(() => {
        if (saveSuccess && isAuthenticated) {
            const fetchAndUpdateWeight = async () => {
                try {
                    const token = await getToken();
                    if (!token) return;
                    const latestWeight = await getLatestWeight(token);
                    if (latestWeight) {
                        setFormData((prev) => ({
                            ...prev,
                            weight: latestWeight.weight || prev.weight,
                        }));
                    }
                } catch (error) {
                    console.warn("Failed to fetch latest weight:", error);
                }
            };
            fetchAndUpdateWeight();
        }
    }, [saveSuccess, isAuthenticated, getToken]);

    const handleSave = useCallback(async () => {
        setSaveError(null);
        setSaveSuccess(false);
        const errors = validateForm(formData);

        if (Object.keys(errors).length > 0) {
            setValidationErrors(errors);
            return;
        }

        setIsSaving(true);
        try {
            const token = await getToken();
            if (!token) throw new Error("Not authenticated");

            // Convert frontend form data to backend request format
            // Backend expects snake_case field names and birth_year (calculated from age)
            const currentYear = new Date().getFullYear();
            const birthYear = currentYear - Number(formData.age);

            // Map activityLevel enum to backend format (convert to lowercase snake_case)
            const activityLevelMap: Record<string, string> = {
                SEDENTARY: "sedentary",
                LIGHTLY_ACTIVE: "light",
                MODERATELY_ACTIVE: "moderate",
                VERY_ACTIVE: "active",
                EXTREMELY_ACTIVE: "very_active",
            };

            const backendRequest = {
                birth_year: birthYear,
                height_cm: Number(formData.height),
                baseline_activity_level: activityLevelMap[formData.activityLevel] || formData.activityLevel,
                gender: formData.gender,
                dietary_preferences: [],
                dietary_restrictions: [],
                fitness_assessment: null,
                fitness_assessment_completed: false,
            };

            await upsertHealthProfile(
                backendRequest as unknown as Partial<HealthProfile>,
                token
            );

            // After successfully saving profile, also record a weight entry with today's date
            try {
                const today = new Date().toISOString().split("T")[0]; // YYYY-MM-DD format
                await recordWeight(
                    { weight: Number(formData.weight), date: today },
                    token
                );
            } catch (weightError) {
                // Log but don't fail the entire save - profile was saved successfully
                console.warn("Failed to record weight entry:", weightError);
            }

            setSaveSuccess(true);
            setIsEditing(false);
            setTimeout(() => setSaveSuccess(false), 3000);
        } catch (error) {
            const message = error instanceof ApiError ? error.message : "Failed to save profile";
            setSaveError(message);
        } finally {
            setIsSaving(false);
        }
    }, [formData, getToken]);

    const clearSuccessMessage = useCallback(() => {
        setSaveSuccess(false);
    }, []);

    return {
        profile: profileQ.data || null,
        formData,
        isEditing,
        isLoading: profileQ.loading,
        isSaving,
        loadError: profileQ.error,
        saveError,
        saveSuccess,
        validationErrors,
        handleInputChange,
        handleEdit,
        handleCancel,
        handleSave,
        clearSuccessMessage,
    };
}
