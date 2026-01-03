import { useEffect, useState } from "react";
import { useAuth0 } from "@auth0/auth0-react";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

type MeResponse = {
  sub: string;
  email?: string;
  aud: string[];
  iss: string;
  scope?: string;
};

export default function Dashboard() {
  const { loginWithRedirect, logout, isAuthenticated, isLoading, getAccessTokenSilently, user } =
    useAuth0();

  const [me, setMe] = useState<MeResponse | null>(null);
  const [error, setError] = useState<string>("");

  useEffect(() => {
    let alive = true;

    (async () => {
      setError("");
      setMe(null);

      if (!isAuthenticated) return;

      try {
        const token = await getAccessTokenSilently();
        const res = await fetch(`${API_BASE_URL}/api/me`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (!res.ok) {
          const text = await res.text().catch(() => "");
          throw new Error(text || `HTTP ${res.status}`);
        }

        const data = (await res.json()) as MeResponse;
        if (alive) setMe(data);
      } catch (e) {
        if (!alive) return;
        setError(e instanceof Error ? e.message : "Unknown error");
      }
    })();

    return () => {
      alive = false;
    };
  }, [isAuthenticated, getAccessTokenSilently]);

  return (
    <div className="p-4 space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <div className="text-lg font-semibold">Dashboard</div>
          <div className="text-sm text-gray-600">
            {isLoading ? "Auth loading..." : isAuthenticated ? `Signed in${user?.email ? `: ${user.email}` : ""}` : "Signed out"}
          </div>
        </div>

        <div className="flex gap-2">
          {!isAuthenticated ? (
            <button
              className="rounded-lg bg-gray-900 px-3 py-2 text-sm font-semibold text-white"
              onClick={() => loginWithRedirect()}
            >
              Log in
            </button>
          ) : (
            <button
              className="rounded-lg border px-3 py-2 text-sm font-semibold"
              onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}
            >
              Log out
            </button>
          )}
        </div>
      </div>

      {error && (
        <div className="rounded-lg border border-amber-300 bg-amber-50 p-3 text-sm">
          <div className="font-semibold">API error</div>
          <div className="text-amber-900 break-words">{error}</div>
        </div>
      )}

      {isAuthenticated && !me && !error && (
        <div className="text-sm text-gray-600">Loading /api/me ...</div>
      )}

      {me && (
        <pre className="rounded-lg border bg-white p-3 text-xs overflow-auto">
          {JSON.stringify(me, null, 2)}
        </pre>
      )}
    </div>
  );
}
