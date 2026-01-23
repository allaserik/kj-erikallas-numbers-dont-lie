import { api } from "../shared/api/client";
import type { SetupStatus } from "./types";

/**
 * API module for setup/onboarding endpoints.
 * 
 * Currently only includes fetching setup status.
 */

export async function getSetupStatus(token: string) {
    return api.get<SetupStatus>(
        "/api/setup/status",
        token
    );
}
