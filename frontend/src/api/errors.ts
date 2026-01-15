import { ApiError } from "../shared/api/client";

export function explainApiError(e: unknown): string {
    if (e instanceof ApiError) {
        if (e.status === 401) return "You are not authorized. Please log in again.";
        if (e.status === 403) return "You do not have permission for this action.";
        if (e.status === 404) return "Data not found. You may need to complete setup.";
        if (e.status === 400) return "Invalid request. Please check your input.";
        if (e.status >= 500) return "Server error. Please try again in a moment.";
        return `Request failed (HTTP ${e.status}).`;
    }
    return e instanceof Error ? e.message : "Unknown error";
}
