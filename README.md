# Numbers Don't Lie

Wellness tracking app with health profile collection, goal tracking, wellness scoring, AI-generated insights, and trend visualizations.

## Project Overview

Main capabilities:
- Account system: email/password, Google OAuth, GitHub OAuth
- Email verification, refresh-token based sessions, password reset
- Optional user-enabled 2FA (TOTP + QR setup)
- Health profile + fitness assessment
- Weight check-ins and historical tracking
- Goals, progress snapshots, and milestone tracking
- BMI + wellness score analytics
- AI insights with caching/fallback and response guardrails
- Privacy preferences, data export, account deletion

## Tech Stack

- Backend: Java, Spring Boot, Spring Security, JPA/Hibernate, Flyway, PostgreSQL
- Frontend: React, TypeScript, Vite
- Auth: Local JWT + Auth0 resource-server validation for OAuth tokens
- AI: OpenAI Responses API (optional), strict JSON schema output

## Quick Start (Docker)

Run full app:

```bash
docker-compose up -d --build
```

Open:
- Frontend: `http://localhost:5173`
- Swagger: `http://localhost:8080/swagger-ui/index.html`

Stop:

```bash
docker-compose down
```

Full reset (drop DB volume):

```bash
docker-compose down -v
docker-compose up -d --build
```

## Demo Mode

Run with pre-seeded demo user/data:

```bash
DEMO_MODE=true VITE_DEMO_MODE=true docker-compose up -d --build
```

Demo credentials:
- `demo@example.com` / `demo@example.com`

Detailed demo instructions:
- [DEMO_MODE.md](DEMO_MODE.md)

## Local Development

1. Start database only:

```bash
docker compose -f ./infra/docker-compose.yml --env-file ./.env up -d
```

2. Start backend:

```bash
cd backend
./mvnw -q -DskipTests compile
./mvnw spring-boot:run
```

3. Start frontend:

```bash
cd frontend
npm install
npm run dev
```

## Environment Variables

Place variables in root `.env` (or export in shell).

Important keys:
- `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`
- `AUTH0_AUDIENCE`
- `OPENAI_API_KEY` (optional)
- `OPENAI_MODEL` (default `gpt-4o-mini`)
- `DEMO_MODE` and `VITE_DEMO_MODE` (for demo data/UI)

If running backend directly and using `.env`:

```bash
set -a
source .env
set +a
```

## AI Insights Notes

When `OPENAI_API_KEY` is configured:
- Insights are generated with strict JSON schema
- Responses are cached by prompt-context hash
- Guardrails enforce grounding/safety/novelty constraints

When AI is unavailable:
- Service returns latest cached insight or a safe fallback insight

## Submission/Review Docs

- Assignment requirements: `docs/ASSIGNMENT.md`
- Test criteria: `docs/ASSIGNMENT_TEST.md`
- Current implementation gap analysis: `docs/REVIEW_GAP_CHECKLIST.md`
- Reviewer test playbook: `docs/REVIEWER_GUIDE.md`
- Encryption-at-rest production plan: `docs/SECURITY_ENCRYPTION_AT_REST_PLAN.md`

## Known Limitations

- Encryption-at-rest is not implemented in application layer (deployment concern)
- Restriction-aware AI filtering is mainly prompt/context driven (not full rule-engine enforcement)
