import { useAuth0 } from "@auth0/auth0-react";
import { DashboardContent } from "./DashboardContent";
import { useDashboardData } from "./useDashboardData";

export function DashboardContainer() {
    const { isAuthenticated } = useAuth0();
    const data = useDashboardData();

    return <DashboardContent isAuthenticated={isAuthenticated} data={data} />;
}
