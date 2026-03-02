import { useState, useCallback } from "react";
import { useAuth0 } from "@auth0/auth0-react";
import { useAuthedQuery } from "../../shared/auth/useAuthedQuery";
import { useAuthToken } from "../../shared/auth/useAuthToken";
import { getHealthProfile, upsertHealthProfile } from "../../shared/api/profile";
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

function initializeFormData(profile: HealthProfile | null): FormData {
    if (profile) {
        return {
            height: profile.height || "",
            weight: profile.weight || "",
            age: profile.age || "",
            gender: profile.gender || "OTHER",
            activityLevel: profile.activityLevel || "MODERATELY_ACTIVE",
            targetWeight: profile.targetWeight || "",
        };
    }
    return {
        height: "",
        weight: "",
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

    // Initialize form with profile data
    const [formData, setFormData] = useState<FormData>(() =>
        initializeFormData(profileQ.data || null)
    );

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
            setFormData(initializeFormData(profileQ.data));
        }
    }, [profileQ.data]);

    const handleCancel = useCallback(() => {
        setIsEditing(false);
        setSaveError(null);
        setValidationErrors({});
    }, []);

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

            await upsertHealthProfile(
                {
                    height: Number(formData.height),
                    weight: Number(formData.weight),
                    age: Number(formData.age),
                    gender: formData.gender,
                    activityLevel: formData.activityLevel,
                    targetWeight: Number(formData.targetWeight),
                } as Partial<HealthProfile>,
                token
            );
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
