# Reviewer Guide

This is a fast, deterministic review script for assignment grading.

## 1. Start Project

```bash
APP_EMAIL_ENABLED=true docker-compose up -d --build
```

URLs:
- Frontend: `http://localhost:5173`
- Backend health: `http://localhost:8080/actuator/health`
- Swagger: `http://localhost:8080/swagger-ui/index.html`
- MailHog inbox: `http://localhost:8025`

Demo mode (recommended):

```bash
DEMO_MODE=true VITE_DEMO_MODE=true APP_EMAIL_ENABLED=true docker-compose up -d --build
```

Demo credentials:
- Email: `demo@example.com`
- Password: `demo@example.com`

## 2. DB Reset (If Needed)

```bash
docker-compose down -v
docker-compose up -d --build
```

Demo reset:

```bash
docker-compose down -v
DEMO_MODE=true VITE_DEMO_MODE=true docker-compose up -d --build
```

## 3. 5-Minute Grading Demo Script

1. Authentication and account security (1 min)
- Login with demo user.
- Open Settings and confirm privacy preferences are available.
- Open 2FA section and confirm optional enable flow with QR is present.

2. Email flows visible to reviewer (45 sec)
- Trigger Forgot Password for a test account.
- Open `http://localhost:8025` and verify reset email appears.
- Register a new email/password account and verify verification email appears.

3. Consent gate and privacy controls (45 sec)
- Disable `data_usage_consent` in Settings.
- Go to Dashboard: AI insight card should show consent-required message and link to Settings.
- Re-enable consent.

4. Health profile and data capture (1 min)
- Open Health Profile and verify demographics, lifestyle, dietary and fitness assessment fields.
- Save profile update and confirm success message appears without reload.

5. Check-in + timeline + activity (1 min)
- In Check In, add weight entry and activity entry.
- Confirm recent timeline shows both events.
- Verify no crash on rapid submissions.

6. Analytics and visualization (1 min)
- Open Dashboard: verify BMI, wellness score, active goal, AI insight, weekly/monthly summaries, and active days (7d) card.
- Open Trends: verify weight chart (with target line if goal target exists), wellness evolution, wellness components, and activity heatmap.
- Switch ranges `30d / 90d / All`.

7. Data rights and reliability (15 sec)
- Trigger data export and verify JSON download includes profile, weights, goals, progress, insights.
- Confirm error states appear inline if any API call fails.

## 4. Mandatory Functional Evidence Map

- Account: email/password + Google + GitHub OAuth, refresh token flow, password reset, optional 2FA.
- Privacy: explicit consent gate, privacy settings persistence, data export, account deletion.
- Health: profile + fitness assessment + weight/activity check-ins + goal progress milestones.
- Analytics: BMI, wellness score with component model, weekly/monthly summaries.
- Visualization: dashboard cards, trends charts, heatmap, loading/error states.
- AI: structured output, validation/guardrails, cached/fallback behavior.

## 5. Known Limitations (Current)

- Full infrastructure-level encryption-at-rest proof is environment-dependent and not fully demonstrated in this repo.
- Dedicated “comparison view” page (current vs target + weekly/monthly in one place) is still partial across dashboard/trends.

## 6. Code Pointers

- Auth/session/security: `backend/src/main/java/com/erikallas/ndl/auth`
- Privacy + consent: `backend/src/main/java/com/erikallas/ndl/privacy`
- Health domain: `backend/src/main/java/com/erikallas/ndl/health`
- AI pipeline: `backend/src/main/java/com/erikallas/ndl/ai`
- Data export: `backend/src/main/java/com/erikallas/ndl/export`
- Frontend features: `frontend/src/features`
- Requirement checklist: `docs/ASSIGNMENT_TEST.md`
