import { useAuth0 } from "@auth0/auth0-react";
import { Card, CardBody, CardTitle } from "../../../shared/ui/Card";

export function AccountSettings() {
    const { logout } = useAuth0();

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
                        onClick={() =>
                            logout({ logoutParams: { returnTo: window.location.origin } })
                        }
                    >
                        Log Out
                    </button>
                </div>
            </CardBody>
        </Card>
    );
}
