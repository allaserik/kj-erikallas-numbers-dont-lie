import { NavLink, Outlet } from "react-router-dom";

const linkBase =
  "flex flex-col items-center justify-center gap-1 text-xs py-2";
const linkInactive = "text-gray-500";
const linkActive = "text-gray-900 font-semibold";

export default function AppShell() {
  return (
    <div className="min-h-dvh bg-gray-50">
      <header className="sticky top-0 z-10 border-b bg-white px-4 py-3">
        <div className="text-base font-semibold">Numbers Don’t Lie</div>
      </header>

      <main className="pb-16">
        <Outlet />
      </main>

      <nav className="fixed bottom-0 left-0 right-0 border-t bg-white">
        <div className="mx-auto grid max-w-md grid-cols-4">
          <NavLink
            to="/"
            end
            className={({ isActive }) =>
              `${linkBase} ${isActive ? linkActive : linkInactive}`
            }
          >
            <span>Today</span>
          </NavLink>

          <NavLink
            to="/checkin"
            className={({ isActive }) =>
              `${linkBase} ${isActive ? linkActive : linkInactive}`
            }
          >
            <span>Log</span>
          </NavLink>

          <NavLink
            to="/trends"
            className={({ isActive }) =>
              `${linkBase} ${isActive ? linkActive : linkInactive}`
            }
          >
            <span>Trends</span>
          </NavLink>

          <NavLink
            to="/settings"
            className={({ isActive }) =>
              `${linkBase} ${isActive ? linkActive : linkInactive}`
            }
          >
            <span>Settings</span>
          </NavLink>
        </div>
      </nav>

    </div>
  );
}
