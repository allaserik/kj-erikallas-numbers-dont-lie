export type AiInsightPayload = {
  recommendations: [string, string, string];
  reflection_question: string;
  summary: string;
};

export type AiInsightResponse = {
  payload: AiInsightPayload;
  source: "openai" | "cache" | "fallback";
  createdAt: string;
};

export type MeResponse = {
  sub: string;
  email?: string;
  aud: string[];
  iss: string;
  scope?: string;
};

/**
 * Setup Status from backend.
 * 
 * isComplete: all requirements met, user can use the app
 * missing: list of what still needs to be set up
 */
export type SetupStatus = {
  isComplete: boolean;
  missing: Array<"profile" | "goal" | "weight">;
};

export type WellnessScoreResponse = {
  wellness_score: number;
  wellness_level: "Excellent" | "Very Good" | "Good" | "Fair" | "Needs Improvement";
};

export type GoalProgressResponse = {
  current_value: number;
  progress_percentage: number;
  is_on_track: boolean;
  days_remaining?: number;
  milestones_completed: number;
  milestone_details?: Array<{ percentage: number; completed_at: string }>;
  recorded_at: string;
  created_at: string;
  updated_at: string;
};