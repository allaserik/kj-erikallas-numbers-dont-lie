import { api } from "../../api/http";

type Goal = { id: string };
type Profile = { heightCm: number };
type WeightEntry = { id: string };

export type SetupStatus = {
  hasProfile: boolean;
  hasActiveGoal: boolean;
  hasWeight: boolean;
  isReady: boolean;
};

export async function getSetupStatus(): Promise<SetupStatus> {
  const [profileOk, goalOk, weightOk] = await Promise.all([
    api<Profile>("/api/profile").then(() => true).catch(() => false),
    api<Goal>("/api/goals/active").then(() => true).catch(() => false),
    api<WeightEntry[]>("/api/weight").then((w) => w.length > 0).catch(() => false),
  ]);

  return {
    hasProfile: profileOk,
    hasActiveGoal: goalOk,
    hasWeight: weightOk,
    isReady: profileOk && goalOk && weightOk,
  };
}
