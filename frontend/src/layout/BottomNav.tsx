import { NavLink } from "react-router-dom";
import {
    HomeIcon,
    PlusCircleIcon,
    ChartBarIcon,
    UserCircleIcon,
} from "@heroicons/react/24/outline";

// BottomNav: Decoupled bottom navigation bar for main app pages.
// Uses Heroicons for modern, accessible icons and NavLink for active styling.
export default function BottomNav() {
    return (
        <nav className="fixed bottom-0 left-0 right-0 bg-white border-t shadow-sm z-10">
            <div className="max-w-md mx-auto flex justify-around py-2">
                {/* Dashboard */}
                <NavLink
                    to="/"
                    end
                    className={({ isActive }) =>
                        `flex flex-col items-center text-xs ${isActive ? "text-blue-600" : "text-gray-500"}`
                    }
                    aria-label="Dashboard"
                >
                    <HomeIcon className="h-6 w-6 mb-0.5" />
                    <span>Dashboard</span>
                </NavLink>
                {/* Check In */}
                <NavLink
                    to="/checkin"
                    className={({ isActive }) =>
                        `flex flex-col items-center text-xs ${isActive ? "text-blue-600" : "text-gray-500"}`
                    }
                    aria-label="Check In"
                >
                    <PlusCircleIcon className="h-6 w-6 mb-0.5" />
                    <span>Check In</span>
                </NavLink>
                {/* Trends */}
                <NavLink
                    to="/trends"
                    className={({ isActive }) =>
                        `flex flex-col items-center text-xs ${isActive ? "text-blue-600" : "text-gray-500"}`
                    }
                    aria-label="Trends"
                >
                    <ChartBarIcon className="h-6 w-6 mb-0.5" />
                    <span>Trends</span>
                </NavLink>
                {/* Profile */}
                <NavLink
                    to="/profile"
                    className={({ isActive }) =>
                        `flex flex-col items-center text-xs ${isActive ? "text-blue-600" : "text-gray-500"}`
                    }
                    aria-label="Profile"
                >
                    <UserCircleIcon className="h-6 w-6 mb-0.5" />
                    <span>Profile</span>
                </NavLink>
            </div>
        </nav>
    );
}
