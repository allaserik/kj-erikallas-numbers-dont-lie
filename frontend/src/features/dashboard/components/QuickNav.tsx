import { Button } from "../../../shared/ui/Button";

export function QuickNav() {
    return (
        <div className="grid grid-cols-2 gap-3">
            <a href="/profile">
                <Button fullWidth className="text-center">
                    👤 Profile
                </Button>
            </a>
            <a href="/goals">
                <Button fullWidth className="text-center">
                    🎯 Goals
                </Button>
            </a>
            <a href="/trends">
                <Button fullWidth className="text-center">
                    📈 Trends
                </Button>
            </a>
            <a href="/checkin">
                <Button fullWidth className="text-center">
                    ✅ Check In
                </Button>
            </a>
        </div>
    );
}
