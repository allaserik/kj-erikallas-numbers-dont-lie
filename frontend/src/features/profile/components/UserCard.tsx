import { useAuth0 } from "@auth0/auth0-react";
import { Card, CardBody } from "../../../shared/ui/Card";

interface UserCardProps {
    isAuthenticated: boolean;
}

export function UserCard({ isAuthenticated }: UserCardProps) {
    const { user } = useAuth0();

    if (!isAuthenticated || !user) {
        return null;
    }

    return (
        <Card>
            <div className="flex flex-col items-center">
                <img
                    src={user.picture}
                    alt={user.name}
                    className="w-16 h-16 rounded-full mb-3 border-2 border-slate-200"
                />
                <div className="font-semibold text-slate-900">{user.name}</div>
                <div className="text-sm text-slate-500">{user.email}</div>
            </div>
        </Card>
    );
}
