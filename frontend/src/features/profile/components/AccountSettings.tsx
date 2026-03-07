import { useAuth0 } from "@auth0/auth0-react";
import { useAppAuth } from "../../../shared/auth/AuthContext";
import { useLocalAuth } from "../../../shared/auth/useLocalAuth";
import { Card, CardBody, CardTitle } from "../../../shared/ui/Card";

export function AccountSettings() {
    const { logout: auth0Logout } = useAuth0();
    const { authMethod } = useAppAuth();
    const { logout: localLogout } = useLocalAuth();

    const handleLogout = () => {
        if (authMethod === 'oauth') {
            // Auth0 logout
            auth0Logout({ logoutParams: { returnTo: window.location.origin } });
        } else if (authMethod === 'local') {
            // Local auth logout
            localLogout();
        }
    };

    return (
        <Card>
            <CardTitle>Account</CardTitle>
            <CardBody>
                <p className="text-sm text-slate-600 mb-4">
                    Manage your account settings and sign out.
                </p>
                <div className="fixed bottom-0 left-0 right-0 md:static md:bg-transparent md:border-0 md:p-0 md:flex md:justify-start bg-white border-t border-slate-200 p-4 flex justify-center z-50 mb-16 md:mb-0">
                    <button
                        className="px-4 py-2 rounded bg-red-600 text-white font-semibold hover:bg-red-700 transition disabled:opacity-50"
                        onClick={handleLogout}
                    >
                        Log Out
                    </button>
                </div>
            </CardBody>
        </Card>
    );
}
