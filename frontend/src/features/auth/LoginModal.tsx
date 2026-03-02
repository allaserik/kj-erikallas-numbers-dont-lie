import { useState } from "react";
import { useAuth0 } from "@auth0/auth0-react";
import { EmailPasswordForm } from "./EmailPasswordForm";

// LoginModal: Full-screen calming overlay for unauthenticated users
// Supports both Auth0 OAuth and email/password authentication
export function LoginModal() {
    const { loginWithRedirect, isLoading } = useAuth0();
    const [authMode, setAuthMode] = useState<'oauth' | 'email'>('oauth');
    const [emailMode, setEmailMode] = useState<'login' | 'register'>('login');

    if (isLoading) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-blue-100/80 backdrop-blur-sm">
            <div className="bg-white rounded-xl shadow-lg p-8 flex flex-col items-center w-full max-w-md">
                {/* App header */}
                <span className="font-bold text-2xl text-green-700 mb-2 select-none">Numbers Don't Lie</span>
                <p className="text-gray-600 mb-6 text-center text-sm">Welcome! Please log in to continue.</p>

                {/* Auth mode tabs */}
                <div className="flex gap-2 mb-6 w-full border-b border-slate-200">
                    <button
                        onClick={() => setAuthMode('oauth')}
                        className={`flex-1 py-3 px-4 text-sm font-semibold transition-colors ${authMode === 'oauth'
                            ? 'border-b-2 border-green-600 text-green-700'
                            : 'text-slate-600 hover:text-slate-800'
                            }`}
                    >
                        OAuth
                    </button>
                    <button
                        onClick={() => setAuthMode('email')}
                        className={`flex-1 py-3 px-4 text-sm font-semibold transition-colors ${authMode === 'email'
                            ? 'border-b-2 border-green-600 text-green-700'
                            : 'text-slate-600 hover:text-slate-800'
                            }`}
                    >
                        Email
                    </button>
                </div>

                {/* Auth mode content */}
                {authMode === 'oauth' ? (
                    <div className="w-full">
                        <button
                            className="w-full px-6 py-3 rounded-lg bg-blue-600 text-white font-semibold shadow hover:bg-blue-700 transition"
                            onClick={() => loginWithRedirect()}
                        >
                            Log In with Google
                        </button>
                        <p className="text-gray-500 text-xs mt-3 text-center">
                            Quick, secure sign-in using your Google account
                        </p>
                    </div>
                ) : (
                    <EmailPasswordForm
                        mode={emailMode}
                        onSuccess={() => {
                            // Modal will automatically disappear when auth state updates
                            // No reload needed - just let the auth state change trigger re-render
                        }}
                        onSwitchMode={() => {
                            setEmailMode(emailMode === 'login' ? 'register' : 'login');
                        }}
                    />
                )}
            </div>
        </div>
    );
}
