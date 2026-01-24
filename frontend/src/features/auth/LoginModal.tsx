import { useState } from "react";
import { Modal } from "../../shared/ui/Modal";
import { LoginForm } from "./LoginForm";

export function LoginModal({ open, onClose }: { open: boolean; onClose: () => void }) {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleLogin = async (email: string, password: string) => {
        setLoading(true);
        setError(null);
        // TODO: Call your backend API for login
        setTimeout(() => {
            setLoading(false);
            // setError("Invalid credentials"); // Example error
            onClose(); // Close modal on success
        }, 1000);
    };

    return (
        <Modal open={open} onClose={onClose} title="Login">
            <LoginForm onSubmit={handleLogin} loading={loading} error={error || undefined} />
        </Modal>
    );
}
