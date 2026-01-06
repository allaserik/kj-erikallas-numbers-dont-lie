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
