# Reviewer Guide

This guide maps the main assignment requirements to a fast, repeatable demo/test flow.

## 1. Run The Project

### One-command Docker run

```bash
docker-compose up -d --build
```

App URLs:
- Frontend: `http://localhost:5173`
- Backend health: `http://localhost:8080/actuator/health`
- Swagger: `http://localhost:8080/swagger-ui/index.html`

### Demo mode (recommended for chart testing)

```bash
DEMO_MODE=true VITE_DEMO_MODE=true docker-compose up -d --build
```

Demo credentials:
- Email: `demo@example.com`
- Password: `demo@example.com`

## 2. Reset Database

Full reset (drop volumes and recreate):

```bash
docker-compose down -v
docker-compose up -d --build
```

Demo reset:

```bash
docker-compose down -v
DEMO_MODE=true VITE_DEMO_MODE=true docker-compose up -d --build
```

## 3. Core Feature Smoke Tests

1. Registration + email verification
- Register with email/password.
- Verify via code/link flow.
- Confirm unverified users are blocked from protected functionality.

2. Authentication methods
- Login with email/password.
- Login with Google OAuth.
- Login with GitHub OAuth.

3. Refresh token
- Login and obtain tokens.
- Trigger expired access token flow.
- Confirm refresh endpoint issues a new access token.

4. Password reset
- Request reset from forgot-password page.
- Use reset link/token to set new password.
- Confirm login works with the new password.

5. Optional 2FA
- Go to Settings -> Account.
- Setup 2FA (QR is shown), enable with TOTP code.
- Confirm login requires valid 2FA code.

## 4. Health + Analytics + Visualization Tests

1. Profile + check-in data
- Fill health profile (demographics, activity, dietary, fitness assessment).
- Add weight entries via Check In.

2. Dashboard
- Confirm BMI, wellness score, active goal, AI insight cards render.
- Confirm weekly/monthly summary cards render.
- Confirm skeleton placeholders while data loads.

3. Trends
- Confirm weight chart renders trend line.
- Confirm target weight reference line appears (when goal has target).
- Confirm milestone markers appear from goal-progress history.
- Confirm 30d/90d/all switch updates chart data.
- Confirm wellness evolution and component charts render.
- Confirm activity heatmap renders.

4. Goal progress + milestones
- Create active goal.
- Record progress repeatedly.
- Confirm progress percentage and milestone history update.

## 5. Privacy, Export, Deletion

1. Consent gating
- Disable data usage consent in Settings.
- Confirm AI insight generation is blocked and UI points to Settings.

2. Privacy preferences
- Toggle anonymized analytics/public profile/email notifications.
- Confirm preferences persist.

3. Data export
- Run export from Settings.
- Confirm JSON includes account/profile/weight/goals/progress/insights with timestamps.

4. Account deletion
- Use delete account flow with confirmation.
- Confirm account and related data are removed/soft-deleted per service behavior.

## 6. AI Reliability Checks

1. Cached fallback
- Disable/misconfigure OpenAI key.
- Confirm API returns cached or fallback insight instead of hard failing.

2. Hallucination guards
- Confirm generated insight is valid JSON schema.
- Confirm no ungrounded percent claims are accepted.
- Confirm unsafe medical/extreme wording is rejected.
- Confirm repeated recommendations are filtered against recent insights.

## 7. Known Limitations (Current)

- Encryption at rest is not implemented at application level (deployment concern).
- Restriction-aware recommendation filtering is mostly prompt/context driven, not a strict rule engine.

## 8. Where To Look In Code

- Auth + sessions: `backend/src/main/java/com/erikallas/ndl/auth`
- Privacy + consent: `backend/src/main/java/com/erikallas/ndl/privacy`
- Health profile/goals/weight/wellness: `backend/src/main/java/com/erikallas/ndl/health`
- AI insight pipeline: `backend/src/main/java/com/erikallas/ndl/ai`
- Data export: `backend/src/main/java/com/erikallas/ndl/export`
- Frontend pages/components: `frontend/src/features`
- Gap checklist: `docs/REVIEW_GAP_CHECKLIST.md`
