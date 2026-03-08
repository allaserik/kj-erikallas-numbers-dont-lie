# Assignment Gap Review (Backend + Frontend)

Date: 2026-03-08
Scope: `docs/ASSIGNMENT.md` + `docs/ASSIGNMENT_TEST.md` vs current codebase
Legend: `DONE` = implemented and visible in code, `PARTIAL` = implemented but incomplete/weak evidence, `MISSING` = not implemented yet

## 1) Core Account Features

| Requirement | Status | Evidence |
|---|---|---|
| Registration with email verification | DONE | `backend/src/main/java/com/erikallas/ndl/auth/api/AuthController.java`, `backend/src/main/java/com/erikallas/ndl/auth/api/EmailVerificationController.java`, `backend/src/main/java/com/erikallas/ndl/auth/email/EmailSender.java` |
| Authentication: email/password + 2 OAuth providers | DONE | `frontend/src/features/auth/LoginModal.tsx` (Google + GitHub), `backend/src/main/java/com/erikallas/ndl/config/MultiIssuerJwtDecoder.java` |
| JWT session management (access/refresh) | DONE | `backend/src/main/java/com/erikallas/ndl/auth/api/RefreshTokenController.java`, `backend/src/main/java/com/erikallas/ndl/auth/service/JwtTokenProvider.java`, `backend/src/main/java/com/erikallas/ndl/auth/service/RefreshTokenService.java` |
| Password reset via email | DONE | `backend/src/main/java/com/erikallas/ndl/auth/api/PasswordResetController.java`, `backend/src/main/java/com/erikallas/ndl/auth/service/PasswordResetService.java` |
| Optional two-factor auth | DONE | `backend/src/main/java/com/erikallas/ndl/auth/api/TwoFactorController.java`, `backend/src/main/java/com/erikallas/ndl/auth/twofactor/TwoFactorService.java`, `frontend/src/features/profile/components/AccountSettings.tsx` |
| Input validation + useful errors | PARTIAL | Validation exists in controllers/forms + global error handler, but inconsistent depth across endpoints (`GlobalExceptionHandler`, manual validation patterns) |

## 2) Health Profile Data Collection

| Requirement | Status | Evidence |
|---|---|---|
| Demographics (age, gender) | DONE | `frontend/src/features/profile/useProfileData.ts`, `backend/src/main/java/com/erikallas/ndl/health/profile/HealthProfileRequest.java` |
| Physical metrics (height, weight) | DONE | `ProfileController`, `WeightController`, `WeightService` |
| Lifestyle (occupation/activity level) | DONE | `fitness_assessment` mapping in `InsightContextBuilder` + profile form fields |
| Dietary preferences/restrictions | DONE | `HealthProfileEntity` (`text[]`), profile form |
| Fitness goals | DONE | Goal module (`GoalController`, `GoalService`, frontend goals pages) |
| Initial fitness assessment fields | DONE | Stored in `fitness_assessment` JSON (`HealthProfileEntity` + form fields in `HealthProfileSection.tsx`) |

## 3) Profile Management

| Requirement | Status | Evidence |
|---|---|---|
| Edit/update health metrics | DONE | `POST /api/profile`, `WeightController` update endpoints, profile/check-in pages |
| View historical changes | PARTIAL | Strong for weight + goal progress history; no dedicated health-profile change history table |
| Export personal data incl. historical metrics | DONE | `backend/src/main/java/com/erikallas/ndl/export/DataExportService.java`, `frontend/src/shared/api/export.ts` |
| Privacy settings + data sharing prefs | DONE | `PrivacyPreferencesController/Service`, `AccountSettings.tsx` toggles for consent/public/email notifications |

## 4) AI Data Preparation + Privacy/Security

| Requirement | Status | Evidence |
|---|---|---|
| Normalized metrics before AI | PARTIAL | Standard units are mostly enforced by model semantics (`kg`, `cm`), but no explicit unit-conversion layer |
| Remove PII before AI | DONE | AI context builder excludes email/name/user identifiers (`InsightContextBuilder`) |
| Structured AI context schema | DONE | Stable context map + prompt builder in `InsightContextBuilder`; strict JSON output schema in `OpenAiClient` |
| Handle missing data gracefully | DONE | Controlled fallbacks in `AiInsightService` (`fallbackToLast`, cached insight fallback) |
| Clear data usage consent | DONE | Consent gate in `AiInsightController` and settings UX in `InsightCard` -> `/settings` |
| Encryption in transit | PARTIAL | Depends on deployment TLS; local env is plain HTTP |
| Encryption at rest | PARTIAL | Production implementation plan documented in `docs/SECURITY_ENCRYPTION_AT_REST_PLAN.md`; full infra/app rollout pending |

## 5) Health Analytics

| Requirement | Status | Evidence |
|---|---|---|
| BMI + classification | DONE | `BMICalculator`, BMI updates in `WeightService` |
| Wellness score 0-100 with weighted formula | DONE | `WellnessScoreCalculator` (30/30/20/20) + `WellnessScoreService` |
| Weekly/monthly progress tracking | PARTIAL | `HealthSummaryController` weekly/monthly exists; activity/habit depth is limited |
| Goal proximity/progress | DONE | `GoalProgressService` calculates progress %, on-track, days remaining |
| Milestone tracking | DONE | `GoalProgressService.checkMilestones()` every 5% |
| Recalculate when inputs change | DONE | Triggered on profile update, weight updates, goal progress record |

## 6) AI Insights Quality/Robustness

| Requirement | Status | Evidence |
|---|---|---|
| Personalized recommendations from profile/history/goals | DONE | Context includes profile + weight trends + goal/progress (`InsightContextBuilder`) |
| Response validation and strict format | DONE | JSON schema at generation + runtime validation in `AiInsightService` |
| Cache/regenerate behavior | DONE | Hash-based cache TTL + fallback to latest cached insight |
| Hallucination handling strategy | DONE | Prompt constraints + server guards (grounding, %-claim gating, safety, anti-repeat) in `AiInsightService` |
| Filter recommendations against restrictions | DONE | Explicit post-generation dietary restriction validation in `AiInsightService` rejects conflicting outputs |

## 7) Data Visualization

| Requirement | Status | Evidence |
|---|---|---|
| Dashboard: BMI, wellness, goals, AI | DONE | `DashboardContent.tsx`, `BMICard`, `WellnessScoreCard`, `GoalCard`, `InsightCard` |
| Weight trend line + target reference + milestones + range switch | DONE | `WeightChart.tsx`, `TrendsPage.tsx`, `useWeightChartData.ts` |
| Wellness evolution line | DONE | `WellnessEvolutionChart.tsx` |
| Wellness component breakdown chart | DONE | `WellnessComponentsStackedChart.tsx` |
| Activity heatmap | DONE | `ActivityHeatmap.tsx` (check-in-frequency based) |
| Comparison views (current vs target, weekly/monthly, trends) | PARTIAL | Present across dashboard/trends; not a unified dedicated comparison view |
| Priority-based AI visualization + expandable details | DONE | Priority labels + `<details>` in `InsightCard.tsx` |
| Loading placeholders and error states | DONE | skeleton cards + alert components on fetch errors |
| Mobile chart resilience | PARTIAL | `overflow-x-auto` present; no formal responsive chart testing evidence |

## 8) Mandatory Test-List Mapping (ASSIGNMENT_TEST)

High-confidence DONE:
- Email verification flow
- Email/password + Google + GitHub auth
- Refresh token flow
- Optional 2FA
- Consent-gated AI generation
- Historical weight timestamps + duplicate timestamp prevention
- Goal milestone tracking
- Cached/fallback AI when provider unavailable
- Dashboard skeleton loading
- Error messages without full page reload
- Health data export
- Rate limiting + 429 handling

Needs explicit reviewer/demo explanation (likely oral):
- PII tradeoff explanation
- Hallucination strategy explanation
- JWT duration security implications
- Normalization impact on visualization accuracy
- BMI-to-wellness contribution rationale
- Model choice / capability tradeoffs
- Caching vs regeneration tradeoff
- Prompt engineering method
- Zero-shot vs few-shot tradeoffs
- Visualization-library tradeoffs
- Missing data impact on AI accuracy

Still weak or incomplete for above-average grading:
- Encryption-at-rest is planned/documented but not yet fully implemented in infra + app runtime

## 9) Recommended Final Pre-Submission Chunks

1. `SHOULD`: Add infra evidence for encryption-at-rest (managed encrypted volume + KMS key policy snippets/screenshots).
2. `SHOULD`: Standardize backend validation style (`@Valid` DTO constraints) across all controllers.
