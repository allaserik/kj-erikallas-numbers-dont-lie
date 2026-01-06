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
