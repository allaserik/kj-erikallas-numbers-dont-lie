import { Outlet } from "react-router-dom";
import BottomNav from "./BottomNav";

// AppShell is a layout route component for React Router v6+.
// It provides the shared app structure (header, nav, etc.) and renders routed content via <Outlet />.
export default function AppShell() {
    return (
        <div className="min-h-screen bg-gray-50 flex flex-col">
            {/* Optional header can go here (e.g., logo, app name, user menu) */}
            <main className="flex-1 max-w-md mx-auto w-full p-4 pb-20">
                {/* Routed page content will be rendered here by React Router */}
                <Outlet />
            </main>
            {/* Decoupled bottom navigation bar */}
            <BottomNav />
        </div>
    );
}
