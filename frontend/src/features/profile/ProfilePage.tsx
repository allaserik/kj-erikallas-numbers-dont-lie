import { useState, useCallback } from "react";
import { useAuth0 } from "@auth0/auth0-react";
import { useAuthedQuery } from "../../shared/auth/useAuthedQuery";
import { getHealthProfile, upsertHealthProfile } from "../../shared/api/profile";
import type { HealthProfile } from "../../shared/types";
import { Button } from "../../shared/ui/Button";
import { Card, CardBody, CardTitle, CardSubtitle } from "../../shared/ui/Card";
import { TextField } from "../../shared/ui/TextField";
import { SelectField } from "../../shared/ui/SelectField";
import { Alert } from "../../shared/ui/Alert";
import { Spinner } from "../../shared/ui/Spinner";
import { ApiError } from "../../shared/api/client";

type FormData = {
    height: number | "";
    weight: number | "";
    age: number | "";
    gender: "MALE" | "FEMALE" | "OTHER";
    activityLevel: "SEDENTARY" | "LIGHTLY_ACTIVE" | "MODERATELY_ACTIVE" | "VERY_ACTIVE" | "EXTREMELY_ACTIVE";
    targetWeight: number | "";
};

type ValidationErrors = Partial<Record<keyof FormData, string>>;

function validateForm(data: FormData): ValidationErrors {
    const errors: ValidationErrors = {};

    if (!data.height || data.height <= 0) errors.height = "Height is required and must be positive";
    if (!data.weight || data.weight <= 0) errors.weight = "Weight is required and must be positive";
    if (!data.age || data.age < 13 || data.age > 120) errors.age = "Age must be between 13 and 120";
    if (!data.targetWeight || data.targetWeight <= 0) errors.targetWeight = "Target weight is required and must be positive";

    return errors;
}

// ProfilePage: View and edit user health profile
export default function ProfilePage() {
    const { user, logout, isAuthenticated } = useAuth0();
    const [isEditing, setIsEditing] = useState(false);
    const [isSaving, setIsSaving] = useState(false);
    const [saveError, setSaveError] = useState<string | null>(null);
    const [saveSuccess, setSaveSuccess] = useState(false);
    const [validationErrors, setValidationErrors] = useState<ValidationErrors>({});

    // Fetch health profile
    const profileQ = useAuthedQuery("healthProfile", getHealthProfile, isAuthenticated && !isEditing);

    // Initialize form with profile data
    const [formData, setFormData] = useState<FormData>(() => {
        if (profileQ.data) {
            return {
                height: profileQ.data.height || "",
                weight: profileQ.data.weight || "",
                age: profileQ.data.age || "",
                gender: profileQ.data.gender || "OTHER",
                activityLevel: profileQ.data.activityLevel || "MODERATELY_ACTIVE",
                targetWeight: profileQ.data.targetWeight || "",
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
    });

    const handleInputChange = useCallback(
        (field: keyof FormData, value: string | number) => {
            setFormData((prev) => ({ ...prev, [field]: value }));
            // Clear error for this field when user starts typing
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

    const handleSave = async () => {
        setSaveError(null);
        setSaveSuccess(false);
        const errors = validateForm(formData);

        if (Object.keys(errors).length > 0) {
            setValidationErrors(errors);
            return;
        }

        setIsSaving(true);
        try {
            await upsertHealthProfile(
                {
                    height: Number(formData.height),
                    weight: Number(formData.weight),
                    age: Number(formData.age),
                    gender: formData.gender,
                    activityLevel: formData.activityLevel,
                    targetWeight: Number(formData.targetWeight),
                } as Partial<HealthProfile>,
                ""
            );
            setSaveSuccess(true);
            setIsEditing(false);
            // Refetch profile
            setTimeout(() => setSaveSuccess(false), 3000);
        } catch (error) {
            const message = error instanceof ApiError ? error.message : "Failed to save profile";
            setSaveError(message);
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <div className="space-y-4 pb-32 md:pb-4">
            <h1 className="text-2xl font-bold text-slate-900">Profile</h1>
            <p className="text-slate-600">Manage your health profile and account settings.</p>

            {/* Auth0 User Card */}
            {isAuthenticated && user && (
                <Card>
                    <div className="flex flex-col items-center">
                        <img
                            src={user.picture}
                            alt={user.name}
                            className="w-16 h-16 rounded-full mb-3 border-2 border-slate-200"
                        />
                        <div className="font-semibold text-slate-900">{user.name}</div>
                        <div className="text-sm text-slate-500">{user.email}</div>
                    </div>
                </Card>
            )}

            {/* Health Profile Form */}
            <Card>
                <CardTitle>Health Profile</CardTitle>
                <CardSubtitle>
                    {isEditing
                        ? "Update your health information"
                        : "Your health metrics and fitness goals"}
                </CardSubtitle>
                <CardBody>
                    {profileQ.loading && <Spinner label="Loading profile..." />}
                    {profileQ.error && (
                        <Alert tone="error" title="Error" message={`Failed to load profile: ${profileQ.error.message}`} />
                    )}

                    {saveError && <Alert tone="error" title="Error" message={saveError} />}
                    {saveSuccess && (
                        <Alert tone="success" title="Success" message="Profile saved successfully!" />
                    )}

                    {!isEditing && profileQ.data ? (
                        // View Mode
                        <div className="space-y-3">
                            <div className="grid grid-cols-2 gap-3">
                                <div className="p-3 bg-slate-50 rounded">
                                    <div className="text-xs text-slate-600">Height</div>
                                    <div className="text-lg font-semibold text-slate-900">
                                        {profileQ.data.height} cm
                                    </div>
                                </div>
                                <div className="p-3 bg-slate-50 rounded">
                                    <div className="text-xs text-slate-600">Current Weight</div>
                                    <div className="text-lg font-semibold text-slate-900">
                                        {profileQ.data.weight} kg
                                    </div>
                                </div>
                                <div className="p-3 bg-slate-50 rounded">
                                    <div className="text-xs text-slate-600">Age</div>
                                    <div className="text-lg font-semibold text-slate-900">
                                        {profileQ.data.age}
                                    </div>
                                </div>
                                <div className="p-3 bg-slate-50 rounded">
                                    <div className="text-xs text-slate-600">Target Weight</div>
                                    <div className="text-lg font-semibold text-slate-900">
                                        {profileQ.data.targetWeight} kg
                                    </div>
                                </div>
                            </div>
                            <div className="grid grid-cols-2 gap-3 text-sm">
                                <div className="p-3 bg-slate-50 rounded">
                                    <div className="text-xs text-slate-600">Gender</div>
                                    <div className="font-medium text-slate-900">
                                        {profileQ.data.gender}
                                    </div>
                                </div>
                                <div className="p-3 bg-slate-50 rounded">
                                    <div className="text-xs text-slate-600">Activity Level</div>
                                    <div className="font-medium text-slate-900">
                                        {profileQ.data.activityLevel.replace(/_/g, " ")}
                                    </div>
                                </div>
                            </div>
                            <Button
                                fullWidth
                                onClick={() => {
                                    setIsEditing(true);
                                    // Update form data from profile
                                    if (profileQ.data) {
                                        setFormData({
                                            height: profileQ.data.height,
                                            weight: profileQ.data.weight,
                                            age: profileQ.data.age,
                                            gender: profileQ.data.gender,
                                            activityLevel: profileQ.data.activityLevel,
                                            targetWeight: profileQ.data.targetWeight,
                                        });
                                    }
                                }}
                            >
                                Edit Profile
                            </Button>
                        </div>
                    ) : (
                        // Edit Mode
                        <div className="space-y-4">
                            <TextField
                                label="Height (cm)"
                                type="number"
                                value={formData.height}
                                onChange={(e) => handleInputChange("height", e.target.value)}
                                error={validationErrors.height}
                                min="50"
                                max="300"
                                placeholder="180"
                            />
                            <TextField
                                label="Current Weight (kg)"
                                type="number"
                                value={formData.weight}
                                onChange={(e) => handleInputChange("weight", e.target.value)}
                                error={validationErrors.weight}
                                min="20"
                                max="300"
                                placeholder="75"
                                step="0.1"
                            />
                            <TextField
                                label="Age"
                                type="number"
                                value={formData.age}
                                onChange={(e) => handleInputChange("age", e.target.value)}
                                error={validationErrors.age}
                                min="13"
                                max="120"
                                placeholder="30"
                            />
                            <SelectField
                                label="Gender"
                                value={formData.gender}
                                onChange={(e) => handleInputChange("gender", e.target.value as FormData["gender"])}
                                options={[
                                    { value: "MALE", label: "Male" },
                                    { value: "FEMALE", label: "Female" },
                                    { value: "OTHER", label: "Other" },
                                ]}
                            />
                            <SelectField
                                label="Activity Level"
                                value={formData.activityLevel}
                                onChange={(e) => handleInputChange("activityLevel", e.target.value as FormData["activityLevel"])}
                                options={[
                                    { value: "SEDENTARY", label: "Sedentary (little exercise)" },
                                    { value: "LIGHTLY_ACTIVE", label: "Lightly active (1-3 days/week)" },
                                    { value: "MODERATELY_ACTIVE", label: "Moderately active (3-5 days/week)" },
                                    { value: "VERY_ACTIVE", label: "Very active (6-7 days/week)" },
                                    { value: "EXTREMELY_ACTIVE", label: "Extremely active (twice per day)" },
                                ]}
                            />
                            <TextField
                                label="Target Weight (kg)"
                                type="number"
                                value={formData.targetWeight}
                                onChange={(e) => handleInputChange("targetWeight", e.target.value)}
                                error={validationErrors.targetWeight}
                                min="20"
                                max="300"
                                placeholder="70"
                                step="0.1"
                            />

                            {/* Edit Form Actions */}
                            <div className="flex gap-2 pt-4">
                                <Button
                                    onClick={handleSave}
                                    disabled={isSaving}
                                    fullWidth
                                >
                                    {isSaving ? "Saving..." : "Save Profile"}
                                </Button>
                                <Button
                                    onClick={() => setIsEditing(false)}
                                    disabled={isSaving}
                                    className="bg-slate-600 hover:bg-slate-700"
                                >
                                    Cancel
                                </Button>
                            </div>
                        </div>
                    )}
                </CardBody>
            </Card>

            {/* Account Settings Section */}
            <Card>
                <CardTitle>Account</CardTitle>
                <CardBody>
                    <p className="text-sm text-slate-600 mb-4">
                        Manage your account settings and sign out.
                    </p>
                    <div className="fixed bottom-0 left-0 right-0 md:static md:!bg-transparent md:!border-0 md:!p-0 md:!flex md:!justify-start bg-white border-t border-slate-200 p-4 flex justify-center z-50 mb-16 md:mb-0">
                        <button
                            className="px-4 py-2 rounded bg-red-600 text-white font-semibold hover:bg-red-700 transition disabled:opacity-50"
                            onClick={() =>
                                logout({ logoutParams: { returnTo: window.location.origin } })
                            }
                        >
                            Log Out
                        </button>
                    </div>
                </CardBody>
            </Card>
        </div>
    );
}
