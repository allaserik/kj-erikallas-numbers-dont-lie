import { api } from "../shared/api/client";

export type MeResponse = {
  sub: string;
  email?: string;
  aud: string[];
  iss: string;
  scope?: string;
  claims?: Record<string, any>;
};

export function getMe(token: string) {
  return api.get<MeResponse>("/api/me", token);
}
