import { useCallback } from "react";
import { useAuth0 } from "@auth0/auth0-react";
import { AUTH0_AUDIENCE } from "../config";

export function useAuthToken() {
  const { isAuthenticated, getAccessTokenSilently } = useAuth0();

  return useCallback(async (): Promise<string | null> => {
    if (!isAuthenticated) return null;

    return getAccessTokenSilently({
      authorizationParams: {
        audience: AUTH0_AUDIENCE,
      },
    });
  }, [isAuthenticated, getAccessTokenSilently]);
}