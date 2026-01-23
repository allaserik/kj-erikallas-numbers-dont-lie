import { Link } from "react-router-dom";
import { Button } from "../../../shared/ui/Button";
import { Card, CardBody, CardSubtitle, CardTitle } from "../../../shared/ui/Card";

export function SetupRequiredCard() {
    return (
        <Card>
            <CardTitle>Finish setup</CardTitle>
            <CardSubtitle>
                We need a bit more information before we can show insights.
            </CardSubtitle>
            <CardBody>
                <Link to="/profile" className="block">
                    <Button variant="primary" fullWidth>
                        Complete profile
                    </Button>
                </Link>
            </CardBody>
        </Card>
    );
}
