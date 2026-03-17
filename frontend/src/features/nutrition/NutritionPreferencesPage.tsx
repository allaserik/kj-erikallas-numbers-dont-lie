import { useEffect, useMemo, useState } from "react";
import { useAppAuth } from "../../shared/auth/AuthContext";
import { useAuthToken } from "../../shared/auth/useAuthToken";
import {
    getNutritionOptions,
    getNutritionPreferences,
    upsertNutritionPreferences,
    type NutritionOptionsResponse,
} from "../../shared/api/nutritionPreferences";
import { Alert } from "../../shared/ui/Alert";
import { Button } from "../../shared/ui/Button";
import { Card, CardBody, CardTitle } from "../../shared/ui/Card";
import { Spinner } from "../../shared/ui/Spinner";
import { TextField } from "../../shared/ui/TextField";

type FormState = {
    timezone: string;
    calorieTargetKcal: string;
    proteinTargetG: string;
    carbsTargetG: string;
    fatsTargetG: string;
    mealsPerDay: string;
    snacksPerDay: string;
    breakfastTime: string;
    lunchTime: string;
    dinnerTime: string;
    dietaryPreferenceCodes: Set<string>;
    allergyCodes: Set<string>;
    dislikedIngredients: string;
    cuisinePreferences: string;
};

const defaultForm: FormState = {
    timezone: "UTC",
    calorieTargetKcal: "",
    proteinTargetG: "",
    carbsTargetG: "",
    fatsTargetG: "",
    mealsPerDay: "3",
    snacksPerDay: "0",
    breakfastTime: "",
    lunchTime: "",
    dinnerTime: "",
    dietaryPreferenceCodes: new Set<string>(),
    allergyCodes: new Set<string>(),
    dislikedIngredients: "",
    cuisinePreferences: "",
};

export default function NutritionPreferencesPage() {
    const { isAuthenticated } = useAppAuth();
    const getToken = useAuthToken();
    const [options, setOptions] = useState<NutritionOptionsResponse | null>(null);
    const [form, setForm] = useState<FormState>(defaultForm);
    const [isLoading, setIsLoading] = useState(true);
    const [isSaving, setIsSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);

    useEffect(() => {
        const load = async () => {
            if (!isAuthenticated) {
                setIsLoading(false);
                return;
            }
            setIsLoading(true);
            setError(null);
            try {
                const token = await getToken();
                if (!token) throw new Error("Not authenticated");
                const [loadedOptions, prefs] = await Promise.all([
                    getNutritionOptions(token),
                    getNutritionPreferences(token),
                ]);
                setOptions(loadedOptions);
                setForm({
                    timezone: prefs.timezone || "UTC",
                    calorieTargetKcal: prefs.calorieTargetKcal?.toString() ?? "",
                    proteinTargetG: prefs.proteinTargetG?.toString() ?? "",
                    carbsTargetG: prefs.carbsTargetG?.toString() ?? "",
                    fatsTargetG: prefs.fatsTargetG?.toString() ?? "",
                    mealsPerDay: prefs.mealsPerDay.toString(),
                    snacksPerDay: prefs.snacksPerDay.toString(),
                    breakfastTime: (prefs.mealTimes?.breakfast as string) || "",
                    lunchTime: (prefs.mealTimes?.lunch as string) || "",
                    dinnerTime: (prefs.mealTimes?.dinner as string) || "",
                    dietaryPreferenceCodes: new Set(prefs.dietaryPreferenceCodes || []),
                    allergyCodes: new Set(prefs.allergyCodes || []),
                    dislikedIngredients: (prefs.dislikedIngredients || []).join(", "),
                    cuisinePreferences: (prefs.cuisinePreferences || []).join(", "),
                });
            } catch (e) {
                const details = e instanceof Error ? e.message : "";
                setError(
                    details
                        ? `Could not load nutrition preferences right now. Please refresh and try again. (${details})`
                        : "Could not load nutrition preferences right now. Please refresh and try again."
                );
            } finally {
                setIsLoading(false);
            }
        };
        load();
    }, [isAuthenticated, getToken]);

    const hasOptions = useMemo(
        () => !!options && options.dietaryPreferences.length > 0 && options.allergies.length > 0,
        [options]
    );

    const toggleCode = (setName: "dietaryPreferenceCodes" | "allergyCodes", code: string) => {
        setForm((prev) => {
            const nextSet = new Set(prev[setName]);
            if (nextSet.has(code)) {
                nextSet.delete(code);
            } else {
                nextSet.add(code);
            }
            return { ...prev, [setName]: nextSet };
        });
    };

    const parseCsv = (value: string) =>
        value
            .split(",")
            .map((v) => v.trim())
            .filter((v) => v.length > 0);

    const onSave = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setSuccess(null);
        setIsSaving(true);
        try {
            const token = await getToken();
            if (!token) throw new Error("Not authenticated");
            await upsertNutritionPreferences(
                {
                    timezone: form.timezone.trim(),
                    calorie_target_kcal: form.calorieTargetKcal ? Number(form.calorieTargetKcal) : null,
                    protein_target_g: form.proteinTargetG ? Number(form.proteinTargetG) : null,
                    carbs_target_g: form.carbsTargetG ? Number(form.carbsTargetG) : null,
                    fats_target_g: form.fatsTargetG ? Number(form.fatsTargetG) : null,
                    meals_per_day: Number(form.mealsPerDay),
                    snacks_per_day: Number(form.snacksPerDay),
                    meal_times: {
                        breakfast: form.breakfastTime || null,
                        lunch: form.lunchTime || null,
                        dinner: form.dinnerTime || null,
                    },
                    dietary_preference_codes: Array.from(form.dietaryPreferenceCodes),
                    allergy_codes: Array.from(form.allergyCodes),
                    disliked_ingredients: parseCsv(form.dislikedIngredients),
                    cuisine_preferences: parseCsv(form.cuisinePreferences),
                },
                token
            );
            setSuccess("Nutrition preferences saved.");
        } catch (e) {
            const details = e instanceof Error ? e.message : "";
            setError(
                details
                    ? `Could not save nutrition preferences. Please check your inputs and try again. (${details})`
                    : "Could not save nutrition preferences. Please check your inputs and try again."
            );
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <div className="space-y-4 pb-32 md:pb-4">
            <div>
                <h1 className="text-2xl font-bold text-slate-900">Nutrition Preferences</h1>
                <p className="text-slate-600">Calorie, macro, meal timing, dietary and cuisine configuration.</p>
            </div>

            {!isAuthenticated && <Alert tone="info" title="Not Authenticated" message="Please log in first." />}
            {isLoading && <Spinner label="Loading nutrition preferences..." />}
            {error && <Alert tone="error" title="Error" message={error} />}
            {success && <Alert tone="success" title="Success" message={success} />}

            {!isLoading && isAuthenticated && (
                <form onSubmit={onSave} className="space-y-4">
                    <Card>
                        <CardTitle>Targets and Structure</CardTitle>
                        <CardBody>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                                <TextField
                                    label="Timezone (IANA)"
                                    value={form.timezone}
                                    onChange={(e) => setForm((prev) => ({ ...prev, timezone: e.target.value }))}
                                    placeholder="Europe/Tallinn"
                                    required
                                />
                                <TextField
                                    label="Calorie target (kcal)"
                                    type="number"
                                    value={form.calorieTargetKcal}
                                    onChange={(e) =>
                                        setForm((prev) => ({ ...prev, calorieTargetKcal: e.target.value }))
                                    }
                                    placeholder="2200"
                                />
                                <TextField
                                    label="Protein target (g)"
                                    type="number"
                                    value={form.proteinTargetG}
                                    onChange={(e) => setForm((prev) => ({ ...prev, proteinTargetG: e.target.value }))}
                                    placeholder="140"
                                />
                                <TextField
                                    label="Carbs target (g)"
                                    type="number"
                                    value={form.carbsTargetG}
                                    onChange={(e) => setForm((prev) => ({ ...prev, carbsTargetG: e.target.value }))}
                                    placeholder="220"
                                />
                                <TextField
                                    label="Fats target (g)"
                                    type="number"
                                    value={form.fatsTargetG}
                                    onChange={(e) => setForm((prev) => ({ ...prev, fatsTargetG: e.target.value }))}
                                    placeholder="70"
                                />
                                <TextField
                                    label="Meals per day"
                                    type="number"
                                    min="1"
                                    max="8"
                                    value={form.mealsPerDay}
                                    onChange={(e) => setForm((prev) => ({ ...prev, mealsPerDay: e.target.value }))}
                                    required
                                />
                                <TextField
                                    label="Snacks per day"
                                    type="number"
                                    min="0"
                                    max="6"
                                    value={form.snacksPerDay}
                                    onChange={(e) => setForm((prev) => ({ ...prev, snacksPerDay: e.target.value }))}
                                    required
                                />
                                <TextField
                                    label="Breakfast time"
                                    type="time"
                                    value={form.breakfastTime}
                                    onChange={(e) => setForm((prev) => ({ ...prev, breakfastTime: e.target.value }))}
                                />
                                <TextField
                                    label="Lunch time"
                                    type="time"
                                    value={form.lunchTime}
                                    onChange={(e) => setForm((prev) => ({ ...prev, lunchTime: e.target.value }))}
                                />
                                <TextField
                                    label="Dinner time"
                                    type="time"
                                    value={form.dinnerTime}
                                    onChange={(e) => setForm((prev) => ({ ...prev, dinnerTime: e.target.value }))}
                                />
                            </div>
                        </CardBody>
                    </Card>

                    <Card>
                        <CardTitle>Dietary Preferences</CardTitle>
                        <CardBody>
                            {!hasOptions && <Alert tone="info" message="No option metadata found yet." />}
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
                                {(options?.dietaryPreferences || []).map((opt) => (
                                    <label key={opt.code} className="flex items-center gap-2 text-sm text-slate-800">
                                        <input
                                            type="checkbox"
                                            checked={form.dietaryPreferenceCodes.has(opt.code)}
                                            onChange={() => toggleCode("dietaryPreferenceCodes", opt.code)}
                                        />
                                        {opt.label}
                                    </label>
                                ))}
                            </div>
                        </CardBody>
                    </Card>

                    <Card>
                        <CardTitle>Allergies and Intolerances</CardTitle>
                        <CardBody>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
                                {(options?.allergies || []).map((opt) => (
                                    <label key={opt.code} className="flex items-center gap-2 text-sm text-slate-800">
                                        <input
                                            type="checkbox"
                                            checked={form.allergyCodes.has(opt.code)}
                                            onChange={() => toggleCode("allergyCodes", opt.code)}
                                        />
                                        {opt.label}
                                    </label>
                                ))}
                            </div>
                        </CardBody>
                    </Card>

                    <Card>
                        <CardTitle>Disliked Ingredients and Cuisines</CardTitle>
                        <CardBody>
                            <div className="grid grid-cols-1 gap-3">
                                <TextField
                                    label="Disliked ingredients (comma-separated)"
                                    value={form.dislikedIngredients}
                                    onChange={(e) =>
                                        setForm((prev) => ({ ...prev, dislikedIngredients: e.target.value }))
                                    }
                                    placeholder="mushrooms, tofu"
                                />
                                <TextField
                                    label="Cuisine preferences (comma-separated)"
                                    value={form.cuisinePreferences}
                                    onChange={(e) =>
                                        setForm((prev) => ({ ...prev, cuisinePreferences: e.target.value }))
                                    }
                                    placeholder="Italian, Mexican"
                                />
                            </div>
                        </CardBody>
                    </Card>

                    <Button type="submit" disabled={isSaving || !isAuthenticated}>
                        {isSaving ? "Saving..." : "Save Nutrition Preferences"}
                    </Button>
                </form>
            )}
        </div>
    );
}
