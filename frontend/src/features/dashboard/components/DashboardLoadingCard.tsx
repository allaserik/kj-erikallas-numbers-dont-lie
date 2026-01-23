import { Card, CardBody, CardTitle } from "../../../shared/ui/Card";
import { Spinner } from "../../../shared/ui/Spinner";

export function DashboardLoadingCard({
    loadingFlags,
}: {
    loadingFlags: {
        me: boolean;
        profile: boolean;
        goal: boolean;
        weights: boolean;
        insight: boolean;
    };
}) {
    return (
        <>
            <Spinner label="Loading dashboard..." />
            <Card>
                <CardTitle>Loading data</CardTitle>
                <CardBody>
                    <ul className="list-disc pl-5 text-sm text-gray-600 space-y-1">
                        {loadingFlags.me && <li>Loading user info...</li>}
                        {loadingFlags.profile && <li>Loading profile...</li>}
                        {loadingFlags.goal && <li>Loading active goal...</li>}
                        {loadingFlags.weights && <li>Loading weight entries...</li>}
                        {loadingFlags.insight && <li>Loading AI insights...</li>}
                    </ul>
                </CardBody>
            </Card>
        </>
    );
}
