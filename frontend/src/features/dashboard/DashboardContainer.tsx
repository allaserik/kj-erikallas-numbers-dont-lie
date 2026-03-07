import { DashboardContent } from "./DashboardContent";
import { useDashboardData } from "./useDashboardData";
import { useAppAuth } from "../../shared/auth/AuthContext";

export function DashboardContainer() {
    const { isAuthenticated } = useAppAuth();
    const data = useDashboardData();

    return <DashboardContent isAuthenticated={isAuthenticated} data={data} />;
}
