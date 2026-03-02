import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Alert } from '../../shared/ui/Alert';
import { Spinner } from '../../shared/ui/Spinner';
import { Card } from '../../shared/ui/Card';

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080';

export function VerifyEmailPage() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    const email = searchParams.get('email');
    const code = searchParams.get('code');

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);

    useEffect(() => {
        const verify = async () => {
            if (!email || !code) {
                setError('Invalid verification link');
                setLoading(false);
                return;
            }

            try {
                const response = await fetch(`${API_BASE}/api/email-verification/verify`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, code }),
                });

                if (!response.ok) {
                    const error = await response.json();
                    throw new Error(error.message || 'Verification failed');
                }

                setSuccess(true);
                // Redirect to login after 2 seconds
                setTimeout(() => navigate('/'), 2000);
            } catch (err) {
                setError(err instanceof Error ? err.message : 'Verification failed');
            } finally {
                setLoading(false);
            }
        };

        verify();
    }, [email, code, navigate]);

    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-b from-blue-50 to-white p-4">
            <Card className="max-w-md w-full">
                <div className="p-8 text-center">
                    {loading && (
                        <>
                            <h2 className="text-2xl font-bold text-slate-800 mb-4">Verifying Email</h2>
                            <Spinner size="lg" className="mx-auto mb-4" />
                            <p className="text-slate-600">Please wait...</p>
                        </>
                    )}

                    {success && (
                        <>
                            <div className="text-4xl mb-4">✓</div>
                            <h2 className="text-2xl font-bold text-green-600 mb-2">Email Verified!</h2>
                            <p className="text-slate-600 mb-4">Your email has been verified successfully.</p>
                            <p className="text-sm text-slate-500">Redirecting to login...</p>
                        </>
                    )}

                    {error && !loading && (
                        <>
                            <h2 className="text-2xl font-bold text-slate-800 mb-4">Verification Failed</h2>
                            <Alert type="error" title="Error" message={error} className="mb-4" />
                            <button
                                onClick={() => navigate('/')}
                                className="text-green-600 hover:text-green-700 font-semibold"
                            >
                                Return to Login
                            </button>
                        </>
                    )}
                </div>
            </Card>
        </div>
    );
}
