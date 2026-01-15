import { api } from "../shared/api/client";

export type AiInsightPayload = {
  recommendations: string[];
  reflection_question: string;
  summary: string;
};

export type AiInsightResponse = {
  payload: AiInsightPayload;
  source: "openai" | "cache" | "fallback";
  createdAt: string;
};

export function getCurrentInsight(token: string) {
  return api.get<AiInsightResponse>("/api/insights/current", token);
}
