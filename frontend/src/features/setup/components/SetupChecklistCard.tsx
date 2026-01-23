import { Link } from "react-router-dom";
import { Button } from "../../../shared/ui/Button";
import { Card, CardBody, CardSubtitle, CardTitle } from "../../../shared/ui/Card";

export function SetupChecklistCard({ missing }: { missing: Array<"profile" | "goal" | "weight"> }) {
    const next =
        missing.includes("profile")
            ? { to: "/profile", label: "Complete profile" }
            : missing.includes("goal")
                ? { to: "/goals", label: "Choose a goal" }
                : { to: "/checkin", label: "Log your weight" };

    return (
        <Card>
            <CardTitle>Setup</CardTitle>
            <CardSubtitle>Complete these steps to unlock the dashboard.</CardSubtitle>
            <CardBody>
                <ul className="list-disc pl-5 text-sm text-gray-700 space-y-1">
                    <li className={missing.includes("profile") ? "" : "text-gray-400"}>
                        Profile (height + activity)
                    </li>
                    <li className={missing.includes("goal") ? "" : "text-gray-400"}>
                        Active goal
                    </li>
                    <li className={missing.includes("weight") ? "" : "text-gray-400"}>
                        At least one weight entry
                    </li>
                </ul>

                <div className="mt-4">
                    <Link to={next.to} className="block">
                        <Button variant="primary" fullWidth>
                            {next.label}
                        </Button>
                    </Link>
                </div>
            </CardBody>
        </Card>
    );
}
