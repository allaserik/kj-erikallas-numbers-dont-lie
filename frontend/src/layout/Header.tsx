// Header: Minimal, centered app name/logo for all screen sizes
// No login/logout button here; focus on brand and calm design
export default function Header() {
    return (
        <header className="w-full bg-blue-50 border-b border-blue-100 shadow-sm sticky top-0 z-30">
            <div className="max-w-md md:max-w-full mx-auto flex items-center justify-center px-4 py-3">
                {/* App name or logo, centered */}
                <span className="font-bold text-xl tracking-tight text-blue-700 select-none">
                    Numbers Don't Lie
                </span>
            </div>
        </header>
    );
}
