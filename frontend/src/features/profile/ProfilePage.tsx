import { useAuth0 } from "@auth0/auth0-react";

// ProfilePage: View and edit user profile and settings
export default function ProfilePage() {
    const { user, logout, isAuthenticated } = useAuth0();

    return (
        <div className="space-y-4 pb-32 md:pb-4"> {/* Add extra bottom padding on mobile */}
            <h1 className="text-2xl font-bold">Profile</h1>
            <p className="text-gray-600">View and edit your profile information here.</p>
            {/* Later: Add profile fields, avatar, and settings links */}

            {/* User info (optional) */}
            {isAuthenticated && user && (
                <div className="bg-white rounded shadow p-4 flex flex-col items-center">
                    <img
                        src={user.picture}
                        alt={user.name}
                        className="w-16 h-16 rounded-full mb-2 border"
                    />
                    <div className="font-semibold">{user.name}</div>
                    <div className="text-sm text-gray-500">{user.email}</div>
                </div>
            )}

            {/* Mobile-only logout button above BottomNav */}
            <div className="fixed bottom-0 left-0 right-0 md:hidden bg-blue-50 border-t border-blue-100 p-4 flex justify-center z-50 mb-16">
                <button
                    className="px-4 py-2 rounded bg-blue-600 text-white font-semibold shadow"
                    onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}
                >
                    Log Out
                </button>
            </div>
        </div>
    );
}
