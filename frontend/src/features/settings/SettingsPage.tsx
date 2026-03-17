import { UserCard } from "../profile/components/UserCard";
import { AccountSettings } from "../profile/components/AccountSettings";
import { useAppAuth } from "../../shared/auth/AuthContext";
import { Card, CardBody, CardTitle } from "../../shared/ui/Card";
import { Link } from "react-router-dom";

export default function SettingsPage() {
    const { isAuthenticated } = useAppAuth();

    return (
        <div className="space-y-4 pb-32 md:pb-4">
            <div>
                <h1 className="text-2xl font-bold text-slate-900">Settings</h1>
                <p className="text-slate-600">Manage account security, privacy, exports, and linked sign-in methods.</p>
            </div>

            <UserCard isAuthenticated={isAuthenticated} />

            <Card>
                <CardTitle>Nutrition</CardTitle>
                <CardBody>
                    <p className="text-sm text-slate-600 mb-3">
                        Configure calorie target, macros, dietary preferences, allergies, meal timing, and cuisine context.
                    </p>
                    <Link
                        to="/planner/nutrition"
                        className="inline-block px-4 py-2 bg-blue-600 text-white rounded font-medium hover:bg-blue-700 transition"
                    >
                        Open Nutrition Preferences
                    </Link>
                </CardBody>
            </Card>

            <AccountSettings />
        </div>
    );
}
