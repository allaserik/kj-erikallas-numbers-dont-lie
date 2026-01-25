import { useAuth0 } from "@auth0/auth0-react";

// AuthButton: Shows login/logout button and user info
// Place this in your Sidebar, AppShell, or ProfilePage for authentication controls
export function AuthButton() {
    const { isAuthenticated, loginWithRedirect, logout, user, isLoading } = useAuth0();

    if (isLoading) return <span>Loading...</span>;

    return (
        <div className="flex items-center gap-2">
            {!isAuthenticated ? (
                <button className="px-3 py-1 rounded bg-blue-600 text-white" onClick={() => loginWithRedirect()}>
                    Log In
                </button>
            ) : (
                <>
                    <span className="text-sm text-gray-700">{user?.name}</span>
                    <button
                        className="px-3 py-1 rounded bg-gray-200 text-gray-800"
                        onClick={() =>
                            logout({ logoutParams: { returnTo: window.location.origin } })
                        }
                    >
                        Log Out
                    </button>
                </>
            )}
        </div>
    );
}
