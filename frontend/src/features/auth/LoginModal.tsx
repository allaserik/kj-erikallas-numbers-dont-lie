import { useAuth0 } from "@auth0/auth0-react";

// LoginModal: Full-screen calming overlay for unauthenticated users
export function LoginModal() {
    const { loginWithRedirect, isLoading } = useAuth0();

    if (isLoading) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-blue-100/80 backdrop-blur-sm">
            <div className="bg-white rounded-xl shadow-lg p-8 flex flex-col items-center max-w-xs w-full">
                {/* App logo or icon can go here */}
                <span className="font-bold text-2xl text-blue-700 mb-2 select-none">Numbers Don't Lie</span>
                <p className="text-gray-600 mb-6 text-center">Welcome! Please log in to continue.</p>
                <button
                    className="px-6 py-2 rounded bg-blue-600 text-white font-semibold text-lg shadow hover:bg-blue-700 transition"
                    onClick={() => loginWithRedirect()}
                >
                    Log In
                </button>
            </div>
        </div>
    );
}
