import { API_BASE_URL } from "../config";

export class ApiError extends Error {
  status: number;
  body?: string;

  constructor(status: number, message: string, body?: string) {
    super(message);
    this.status = status;
    this.body = body;
  }
}

type ApiOptions = RequestInit & {
  token?: string | null;
};

export async function apiFetch<T>(path: string, options: ApiOptions = {}): Promise<T> {
  const url = `${API_BASE_URL}${path}`;

  const headers = new Headers(options.headers);
  if (!headers.has("Content-Type")) headers.set("Content-Type", "application/json");
  if (options.token) headers.set("Authorization", `Bearer ${options.token}`);

  const res = await fetch(url, {
    ...options,
    headers,
  });

  if (!res.ok) {
    const body = await res.text().catch(() => "");
    // Keep message concise but useful
    throw new ApiError(res.status, body || `HTTP ${res.status}`, body);
  }

  // Support 204 No Content
  if (res.status === 204) return undefined as unknown as T;

  return (await res.json()) as T;
}

// Convenience helpers (optional but useful)
export const api = {
  get: <T>(path: string, token?: string | null) => apiFetch<T>(path, { method: "GET", token }),
  post: <T>(path: string, body: unknown, token?: string | null) =>
    apiFetch<T>(path, { method: "POST", body: JSON.stringify(body), token }),
  put: <T>(path: string, body: unknown, token?: string | null) =>
    apiFetch<T>(path, { method: "PUT", body: JSON.stringify(body), token }),
  del: <T>(path: string, token?: string | null) => apiFetch<T>(path, { method: "DELETE", token }),
};
