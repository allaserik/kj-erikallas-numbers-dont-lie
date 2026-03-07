import { useAuthedQuery } from "../../shared/auth/useAuthedQuery";
import { useAppAuth } from "../../shared/auth/AuthContext";
import { getMe } from "../../shared/api/profile";
import { Card, CardBody, CardTitle } from "../../shared/ui/Card";
import { Alert } from "../../shared/ui/Alert";
import { Spinner } from "../../shared/ui/Spinner";
import type { UserProfile } from "../../shared/types";

export default function SettingsPage() {
    const { isAuthenticated } = useAppAuth();
    const meQ = useAuthedQuery("me", getMe, isAuthenticated);

    const userProfile: UserProfile | null = meQ.data || null;

    return (
        <div className="space-y-4 pb-32 md:pb-4 max-w-2xl mx-auto">
            <div>
                <h1 className="text-2xl font-bold text-slate-900">Settings</h1>
                <p className="text-slate-600">Manage your account and preferences</p>
            </div>

            {meQ.loading && (
                <div className="flex justify-center py-12">
                    <Spinner />
                </div>
            )}

            {meQ.error && (
                <Alert
                    tone="error"
                    title="Error Loading Account"
                    message={meQ.error.message || "Failed to load account information"}
                />
            )}

            {!meQ.loading && !meQ.error && (
                <div className="space-y-4">
                    {/* Account Information */}
                    <Card>
                        <CardTitle>🔐 Account Information</CardTitle>
                        <CardBody>
                            <div className="space-y-4">
                                {/* Email */}
                                <div className="border-b border-slate-200 pb-4">
                                    <label className="block text-sm font-medium text-slate-700 mb-1">
                                        Email Address
                                    </label>
                                    <div className="flex items-center justify-between">
                                        <div className="flex items-center gap-3">
                                            <input
                                                type="email"
                                                value={userProfile?.email || auth0User?.email || ""}
                                                disabled
                                                className="px-3 py-2 bg-slate-50 border border-slate-300 rounded text-slate-900 font-mono text-sm cursor-not-allowed"
                                            />
                                            {auth0User?.sub && auth0User.sub.startsWith("google-oauth2") && (
                                                <span className="px-2 py-1 bg-blue-100 text-blue-800 text-xs font-medium rounded-full whitespace-nowrap">
                                                    🔗 Gmail
                                                </span>
                                            )}
                                        </div>
                                    </div>
                                    <p className="text-xs text-slate-500 mt-2">
                                        {auth0User?.email_verified
                                            ? "✓ Verified"
                                            : "● Pending verification"}
                                    </p>
                                </div>

                                {/* Auth Provider */}
                                <div className="pb-4">
                                    <label className="block text-sm font-medium text-slate-700 mb-2">
                                        Authentication Method
                                    </label>
                                    <div className="p-3 bg-blue-50 border border-blue-200 rounded">
                                        <p className="text-sm text-blue-900">
                                            {auth0User?.sub?.startsWith("google-oauth2")
                                                ? "Signed in with Google"
                                                : "Signed in with Auth0"}
                                        </p>
                                    </div>
                                </div>

                                {/* Account Created */}
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">
                                        Account Created
                                    </label>
                                    <p className="text-2 text-slate-600">
                                        {userProfile?.createdAt
                                            ? new Date(userProfile.createdAt).toLocaleDateString("en-US", {
                                                year: "numeric",
                                                month: "long",
                                                day: "numeric",
                                            })
                                            : "—"}
                                    </p>
                                </div>
                            </div>
                        </CardBody>
                    </Card>

                    {/* Privacy Notice */}
                    <Card>
                        <CardBody>
                            <p className="text-sm text-slate-600">
                                <strong>🛡️ Privacy:</strong> Your email is never shared with third parties. It's used
                                only to send password reset links and important account notifications.
                            </p>
                        </CardBody>
                    </Card>
                </div>
            )}
        </div>
    );
}
