export async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
  const res = await fetch(`http://localhost:8080${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(init.headers ?? {}),
    },
    credentials: "include", // keep if you use cookies; harmless otherwise
  });

  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new Error(`${res.status} ${res.statusText}${text ? `: ${text}` : ""}`);
  }

  return res.json() as Promise<T>;
}
