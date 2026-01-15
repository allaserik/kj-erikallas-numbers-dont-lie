import { api } from "../shared/api/client";

export type ProfileResponse = {
  userId: string;
  birthYear: number;
  gender: string;
  heightCm: number;
  baselineActivityLevel: string;
  createdAt: string;
  updatedAt: string;
};

export function getProfile(token: string) {
  return api.get<ProfileResponse>("/api/profile", token);
}