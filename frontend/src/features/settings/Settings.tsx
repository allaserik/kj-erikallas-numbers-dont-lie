import { Link } from "react-router-dom";
import { Card, CardBody, CardSubtitle, CardTitle } from "../../shared/ui/Card";
import { Button } from "../../shared/ui/Button";

export default function Settings() {
    return (
        <div className="p-4 space-y-4">
            <Card>
                <CardTitle>Settings</CardTitle>
                <CardSubtitle>Profile, goals, and app configuration.</CardSubtitle>

                <CardBody>
                    <div className="space-y-3">
                        <Link to="/profile" className="block">
                            <Button fullWidth>Profile</Button>
                        </Link>

                        <Link to="/goals" className="block">
                            <Button fullWidth>Goals</Button>
                        </Link>

                        {/* Future */}
                        <div className="text-xs text-gray-600">
                            AI provider/model/prompt editing will live here later (admin-only options can be added).
                        </div>
                    </div>
                </CardBody>
            </Card>
        </div>
    );
}
// ...existing code from pages/Settings.tsx will be moved here
