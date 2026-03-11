# Project Review Checklist (60-90 Minutes)

Use this before submission or review to ensure you can defend decisions, demonstrate flows, and map implementation to requirements.

## 1. Problem + Scope (10 min)

- [ ] I can explain the project goal in 2 sentences.
- [ ] I can list what is in scope vs out of scope.
- [ ] I can name 2 tradeoffs I chose and why.

## 2. Architecture Snapshot (10 min)

- [ ] I can sketch modules/components and data flow.
- [ ] I can explain why this architecture fits requirements and timeline.
- [ ] I can name 1 alternative approach and why I did not use it.

## 3. Data Model Confidence (10 min)

- [ ] I can explain the purpose of each core table/entity.
- [ ] I can explain 2 key relationships.
- [ ] I can explain 1 data integrity rule (constraint/index/validation).
- [ ] I can explain date/time and unit conventions.

## 4. API Contract Confidence (10 min)

- [ ] I know top 5 endpoints and their request/response shape.
- [ ] I can explain input validation and common error responses.
- [ ] I can explain at least 1 auth/permission rule.

## 5. Critical User Flows (15 min)

- [ ] Happy path flow (end-to-end) is clear.
- [ ] Edit/update flow (end-to-end) is clear.
- [ ] Failure/recovery flow (end-to-end) is clear.
- [ ] For each flow, I can state:
  - frontend action,
  - backend behavior,
  - visible user result.

## 6. AI/Logic Reliability (10 min)

- [ ] I can explain where AI is used vs deterministic logic.
- [ ] I can explain fallback when AI/external API fails.
- [ ] I can explain at least 1 output validation or anti-hallucination mechanism.

## 7. UI/UX Rationale (10 min)

- [ ] I can justify navigation/page ownership choices.
- [ ] I can explain mobile-first decisions for key actions.
- [ ] I can show loading/empty/error states for key screens.

## 8. Requirement Mapping (10 min)

- [ ] I reviewed the project assignment test file line-by-line.
- [ ] I marked each requirement as done / partial / missing.
- [ ] For each "done", I have one proof point:
  - endpoint,
  - screen,
  - test,
  - or log evidence.

## 9. Defense Notes (5 min)

- [ ] I prepared a short answer for:
  - hardest technical decision and why,
  - biggest risk and mitigation,
  - what I would improve next with more time.

## 10. Demo Script (5 min)

- [ ] I prepared a fixed 5-7 step demo sequence.
- [ ] Demo includes at least:
  - one create/update flow,
  - one analytics/visualization view,
  - one failure/fallback behavior.
- [ ] Demo avoids dead ends and ambiguous navigation.

## Quick Output (for yourself)

Write this before finishing review:

- Project status: `ready` / `mostly ready` / `needs fixes`
- Top 3 risks still open:
  1.
  2.
  3.
- Mandatory fixes before submission:
  1.
  2.
  3.
