import { Card, CardBody, CardTitle, CardSubtitle } from "../../../shared/ui/Card";
import { TextField } from "../../../shared/ui/TextField";
import { SelectField } from "../../../shared/ui/SelectField";
import { Alert } from "../../../shared/ui/Alert";
import { Button } from "../../../shared/ui/Button";
import { Spinner } from "../../../shared/ui/Spinner";
import type { FormData, ValidationErrors } from "../useProfileData";
import type { HealthProfile } from "../../../shared/types";

interface HealthProfileSectionProps {
    profile: HealthProfile | null;
    isLoading: boolean;
    isEditing: boolean;
    isSaving: boolean;
    formData: FormData;
    loadError: Error | null;
    saveError: string | null;
    saveSuccess: boolean;
    validationErrors: ValidationErrors;
    onInputChange: (field: keyof FormData, value: string | number) => void;
    onEdit: () => void;
    onSave: () => Promise<void>;
    onCancel: () => void;
}

export function HealthProfileSection({
    profile,
    isLoading,
    isEditing,
    isSaving,
    formData,
    loadError,
    saveError,
    saveSuccess,
    validationErrors,
    onInputChange,
    onEdit,
    onSave,
    onCancel,
}: HealthProfileSectionProps) {
    return (
        <Card>
            <CardTitle>Health Profile</CardTitle>
            <CardSubtitle>
                {isEditing ? "Update your health information" : "Your health metrics and fitness goals"}
            </CardSubtitle>
            <CardBody>
                {isLoading && <Spinner label="Loading profile..." />}
                {loadError && (
                    <Alert tone="error" title="Error" message={`Failed to load profile: ${loadError.message}`} />
                )}

                {saveError && <Alert tone="error" title="Error" message={saveError} />}
                {saveSuccess && (
                    <Alert tone="success" title="Success" message="Profile saved successfully!" />
                )}

                {!isEditing && profile ? (
                    // View Mode
                    <div className="space-y-3">
                        <div className="grid grid-cols-2 gap-3">
                            <div className="p-3 bg-slate-50 rounded">
                                <div className="text-xs text-slate-600">Height</div>
                                <div className="text-lg font-semibold text-slate-900">
                                    {profile.height} cm
                                </div>
                            </div>
                            <div className="p-3 bg-slate-50 rounded">
                                <div className="text-xs text-slate-600">Current Weight</div>
                                <div className="text-lg font-semibold text-slate-900">
                                    {profile.weight} kg
                                </div>
                            </div>
                            <div className="p-3 bg-slate-50 rounded">
                                <div className="text-xs text-slate-600">Age</div>
                                <div className="text-lg font-semibold text-slate-900">
                                    {profile.age}
                                </div>
                            </div>
                            <div className="p-3 bg-slate-50 rounded">
                                <div className="text-xs text-slate-600">Target Weight</div>
                                <div className="text-lg font-semibold text-slate-900">
                                    {profile.targetWeight} kg
                                </div>
                            </div>
                        </div>
                        <div className="grid grid-cols-2 gap-3 text-sm">
                            <div className="p-3 bg-slate-50 rounded">
                                <div className="text-xs text-slate-600">Gender</div>
                                <div className="font-medium text-slate-900">
                                    {profile.gender}
                                </div>
                            </div>
                            <div className="p-3 bg-slate-50 rounded">
                                <div className="text-xs text-slate-600">Activity Level</div>
                                <div className="font-medium text-slate-900">
                                    {profile.activityLevel ? profile.activityLevel.replace(/_/g, " ") : "—"}
                                </div>
                            </div>
                        </div>
                        <Button fullWidth onClick={onEdit}>
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
                            onChange={(e) => onInputChange("height", e.target.value)}
                            error={validationErrors.height}
                            min="50"
                            max="300"
                            placeholder="180"
                        />
                        <TextField
                            label="Current Weight (kg)"
                            type="number"
                            value={formData.weight}
                            onChange={(e) => onInputChange("weight", e.target.value)}
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
                            onChange={(e) => onInputChange("age", e.target.value)}
                            error={validationErrors.age}
                            min="13"
                            max="120"
                            placeholder="30"
                        />
                        <SelectField
                            label="Gender"
                            value={formData.gender}
                            onChange={(e) =>
                                onInputChange("gender", e.target.value as FormData["gender"])
                            }
                            options={[
                                { value: "MALE", label: "Male" },
                                { value: "FEMALE", label: "Female" },
                                { value: "OTHER", label: "Other" },
                            ]}
                        />
                        <SelectField
                            label="Activity Level"
                            value={formData.activityLevel}
                            onChange={(e) =>
                                onInputChange(
                                    "activityLevel",
                                    e.target.value as FormData["activityLevel"]
                                )
                            }
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
                            onChange={(e) => onInputChange("targetWeight", e.target.value)}
                            error={validationErrors.targetWeight}
                            min="20"
                            max="300"
                            placeholder="70"
                            step="0.1"
                        />

                        {/* Edit Form Actions */}
                        <div className="flex gap-2 pt-4">
                            <Button onClick={onSave} disabled={isSaving} fullWidth>
                                {isSaving ? "Saving..." : "Save Profile"}
                            </Button>
                            <Button
                                onClick={onCancel}
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
    );
}
