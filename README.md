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

Bonus/extra functionality implemented:
- Multi-provider account identity linking with email-collision guidance
- AI guardrails (grounding, safety checks, anti-repeat, restriction checks)
- Demo data mode for end-to-end visualization testing
- Application-layer at-rest protection for sensitive auth secrets

## Tech Stack

- Backend: Java, Spring Boot, Spring Security, JPA/Hibernate, Flyway, PostgreSQL
- Frontend: React, TypeScript, Vite
- Auth: Local JWT + Auth0 resource-server validation for OAuth tokens
- AI: OpenAI Responses API (optional), strict JSON schema output

## Setup And Installation

1. Copy env template:

```bash
cp .env.example .env
```

2. Set secure values in `.env`:
- `APP_TOKEN_PEPPER`
- `APP_DATA_ENCRYPTION_KEY`

Generate them with:

```bash
openssl rand -base64 48
openssl rand -base64 48
```

Use first output as `APP_TOKEN_PEPPER`, second as `APP_DATA_ENCRYPTION_KEY`.

## One-Line Run (Docker)

After `.env` is prepared:

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

Reset database:

```bash
docker-compose down -v
docker-compose up -d --build
```

## Usage Guide

Typical user flow:
1. Sign up/login (email/password, Google, or GitHub)
2. Verify email (for local email/password auth)
3. Complete health profile
4. Add weight check-ins
5. Create an active goal
6. View dashboard (BMI, wellness score, summaries, AI insight)
7. View trends (weight line, wellness evolution, component breakdown, heatmap)
8. Use settings for privacy consent, 2FA, export, and account deletion

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

Use root `.env` for normal project runs.

Important keys:
- `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`
- `AUTH0_AUDIENCE`
- `OPENAI_API_KEY` (optional)
- `OPENAI_MODEL` (default `gpt-4o-mini`)
- `DEMO_MODE` and `VITE_DEMO_MODE` (optional demo flags)
- `APP_TOKEN_PEPPER` (pepper for hashing refresh/reset/verification tokens at rest)
- `APP_DATA_ENCRYPTION_KEY` (AES key material for encrypting 2FA secrets at rest)

If running backend directly and loading `.env` into shell:

```bash
set -a
source .env
set +a
```

## Which `.env` Files Are Needed?

- Required: `.env` (root)
- Template: `.env.example`

For submission and one-line run, only `.env` is required.

## AI Insights Notes

When `OPENAI_API_KEY` is configured:
- Insights are generated with strict JSON schema
- Responses are cached by prompt-context hash
- Guardrails enforce grounding/safety/novelty/restriction constraints

When AI is unavailable:
- Service returns latest cached insight or a safe fallback insight

## Submission/Review Docs

- Assignment requirements: `docs/ASSIGNMENT.md`
- Test criteria: `docs/ASSIGNMENT_TEST.md`
- Current implementation gap analysis: `docs/REVIEW_GAP_CHECKLIST.md`
- Reviewer test playbook: `docs/REVIEWER_GUIDE.md`
- Requirements defense notes: `docs/REQUIREMENTS_FULFILLMENT_EXPLANATIONS.md`
- Encryption-at-rest production plan: `docs/SECURITY_ENCRYPTION_AT_REST_PLAN.md`

## Known Limitations

- Infra-level encryption-at-rest evidence (volume/KMS policy) is still pending
- Restriction-aware AI filtering is implemented, but currently keyword-based rather than a full nutrition rule engine
