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