import { Alert } from "../../../shared/ui/Alert";

export function DashboardErrorAlert({ message }: { message: string }) {
    return <Alert title="API error" message={message} tone="warning" />;
}
