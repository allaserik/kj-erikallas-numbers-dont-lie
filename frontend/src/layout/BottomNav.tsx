import { NavLink } from "react-router-dom";
import {
    HomeIcon,
    PlusCircleIcon,
    ChartBarIcon,
    UserCircleIcon,
} from "@heroicons/react/24/outline";

// BottomNav: Mobile navigation with neutral palette. Soft green accents for active tabs.
// Modern, minimal styling with good touch targets.
export default function BottomNav() {
    return (
        <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-slate-200 z-10">
            <div className="max-w-md mx-auto flex justify-around py-2">
                {/* Dashboard */}
                <NavLink
                    to="/"
                    end
                    className={({ isActive }) =>
                        `flex flex-col items-center gap-0.5 px-3 py-1.5 text-xs rounded-md transition-colors ${isActive ? "text-green-700 bg-green-50" : "text-slate-600"}`
                    }
                    aria-label="Dashboard"
                >
                    <HomeIcon className="h-6 w-6" />
                    <span>Dashboard</span>
                </NavLink>
                {/* Check In */}
                <NavLink
                    to="/checkin"
                    className={({ isActive }) =>
                        `flex flex-col items-center gap-0.5 px-3 py-1.5 text-xs rounded-md transition-colors ${isActive ? "text-green-700 bg-green-50" : "text-slate-600"}`
                    }
                    aria-label="Check In"
                >
                    <PlusCircleIcon className="h-6 w-6" />
                    <span>Check In</span>
                </NavLink>
                {/* Trends */}
                <NavLink
                    to="/trends"
                    className={({ isActive }) =>
                        `flex flex-col items-center gap-0.5 px-3 py-1.5 text-xs rounded-md transition-colors ${isActive ? "text-green-700 bg-green-50" : "text-slate-600"}`
                    }
                    aria-label="Trends"
                >
                    <ChartBarIcon className="h-6 w-6" />
                    <span>Trends</span>
                </NavLink>
                {/* Profile */}
                <NavLink
                    to="/profile"
                    className={({ isActive }) =>
                        `flex flex-col items-center gap-0.5 px-3 py-1.5 text-xs rounded-md transition-colors ${isActive ? "text-green-700 bg-green-50" : "text-slate-600"}`
                    }
                    aria-label="Profile"
                >
                    <UserCircleIcon className="h-6 w-6" />
                    <span>Profile</span>
                </NavLink>
            </div>
        </nav>
    );
}
