import { useCallback } from "react";
import { useAuth0 } from "@auth0/auth0-react";
import { useAuthToken } from "./useAuthToken";
import { useApiQuery } from "../../hooks/useApiQuery";

export function useAuthedQuery<T>(key: string, fn: (token: string) => Promise<T>, enabledOverride?: boolean) {
  const { isAuthenticated } = useAuth0();
  const getToken = useAuthToken();
  const enabled = enabledOverride ?? isAuthenticated;

  const runner = useCallback(async () => {
    const token = await getToken();
    if (!token) throw new Error("Not authenticated");
    return fn(token);
  }, [fn, getToken]);

  return useApiQuery(key,runner, enabled);
}
