import { Outlet } from "react-router-dom";
import BottomNav from "./BottomNav";
import Sidebar from "./Sidebar";

// AppShell is a layout route component for React Router v6+.
// It provides the shared app structure (header, nav, etc.) and renders routed content via <Outlet />.
export default function AppShell() {
    return (
        <div className="min-h-screen bg-gray-50 flex flex-col md:flex-row">
            {/* Sidebar: visible on md+ screens, hidden on mobile */}
            <Sidebar />
            {/* Main content area: margin-left for sidebar on md+ screens */}
            <div className="flex-1 max-w-md md:max-w-full mx-auto w-full p-4 pb-20 md:ml-56">
                {/* Routed page content will be rendered here by React Router */}
                <Outlet />
            </div>
            {/* BottomNav: visible on mobile, hidden on md+ screens */}
            <div className="md:hidden">
                <BottomNav />
            </div>
        </div>
    );
}
