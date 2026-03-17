import { Link, Outlet, useLocation } from "react-router-dom";

const tabs = [
    { to: "/planner/today", label: "Today" },
    { to: "/planner/goals", label: "Goals" },
    { to: "/planner/nutrition", label: "Nutrition" },
];

export default function PlannerPage() {
    const location = useLocation();

    const isActive = (to: string) => location.pathname === to;

    return (
        <div className="space-y-4 pb-32 md:pb-4">
            <div>
                <h1 className="text-2xl font-bold text-slate-900">Planner</h1>
                <p className="text-slate-600">Manage your daily check-ins, goals, and nutrition planning context.</p>
            </div>

            <div className="inline-flex rounded-lg border border-slate-200 bg-white p-1">
                {tabs.map((tab) => (
                    <Link
                        key={tab.to}
                        to={tab.to}
                        className={`px-3 py-1.5 text-sm rounded-md transition ${isActive(tab.to)
                            ? "bg-green-600 text-white"
                            : "text-slate-700 hover:bg-slate-100"
                            }`}
                    >
                        {tab.label}
                    </Link>
                ))}
            </div>

            <Outlet />
        </div>
    );
}
