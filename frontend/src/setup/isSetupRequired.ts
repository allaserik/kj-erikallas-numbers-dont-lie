import { ApiError } from "../shared/api/client";

export function isSetupRequired(profileError: Error | null): boolean {
    if (!profileError) return false;
    if (profileError instanceof ApiError && profileError.status === 404) return true;
    return false;
}
