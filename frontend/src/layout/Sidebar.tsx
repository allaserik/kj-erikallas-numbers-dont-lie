import { NavLink } from "react-router-dom";
import {
    HomeIcon,
    PlusCircleIcon,
    ChartBarIcon,
    UserCircleIcon,
} from "@heroicons/react/24/outline";
import { AuthButton } from "../features/auth/AuthButton";

// Sidebar: Responsive sidebar for desktop/tablet navigation.
// Hidden on mobile, visible on md+ screens. AuthButton at the bottom.
export default function Sidebar() {
    return (
        <aside className="hidden md:flex md:flex-col md:w-56 md:h-screen md:fixed md:left-0 md:top-0 bg-blue-50 border-r border-blue-100 shadow-sm z-20">
            <div className="flex flex-col gap-2 p-4 flex-1">
                <NavLink
                    to="/"
                    end
                    className={({ isActive }) =>
                        `flex items-center gap-2 px-3 py-2 rounded-md text-sm font-medium transition-colors ${isActive ? "bg-blue-100 text-blue-700" : "text-gray-700 hover:bg-blue-100"}`
                    }
                >
                    <HomeIcon className="h-5 w-5" /> Dashboard
                </NavLink>
                <NavLink
                    to="/checkin"
                    className={({ isActive }) =>
                        `flex items-center gap-2 px-3 py-2 rounded-md text-sm font-medium transition-colors ${isActive ? "bg-blue-100 text-blue-700" : "text-gray-700 hover:bg-blue-100"}`
                    }
                >
                    <PlusCircleIcon className="h-5 w-5" /> Check In
                </NavLink>
                <NavLink
                    to="/trends"
                    className={({ isActive }) =>
                        `flex items-center gap-2 px-3 py-2 rounded-md text-sm font-medium transition-colors ${isActive ? "bg-blue-100 text-blue-700" : "text-gray-700 hover:bg-blue-100"}`
                    }
                >
                    <ChartBarIcon className="h-5 w-5" /> Trends
                </NavLink>
                <NavLink
                    to="/profile"
                    className={({ isActive }) =>
                        `flex items-center gap-2 px-3 py-2 rounded-md text-sm font-medium transition-colors ${isActive ? "bg-blue-100 text-blue-700" : "text-gray-700 hover:bg-blue-100"}`
                    }
                >
                    <UserCircleIcon className="h-5 w-5" /> Profile
                </NavLink>
            </div>
            {/* Auth controls at the bottom */}
            <div className="p-4 border-t border-blue-100">
                <AuthButton />
            </div>
        </aside>
    );
}
