import { apiFetch } from "./client";

export async function deleteMyAccount(token: string, confirmation: string): Promise<void> {
    await apiFetch<void>("/api/account", {
        method: "DELETE",
        token,
        body: JSON.stringify({ confirmation }),
    });
}
