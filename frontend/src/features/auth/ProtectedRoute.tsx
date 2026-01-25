import { useAuth0 } from "@auth0/auth0-react";
import { Navigate } from "react-router-dom";
import React from "react";

// ProtectedRoute: Restricts access to authenticated users only
export function ProtectedRoute({ children }: { children: React.ReactNode }) {
    const { isAuthenticated, isLoading } = useAuth0();
    if (isLoading) return <div>Loading...</div>;
    return isAuthenticated ? <>{children}</> : <Navigate to="/" />;
}
