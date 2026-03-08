# Demo Mode Setup Guide

## Overview

Demo mode allows you to run the application with pre-populated sample data for testing, visualization, and demonstrations. This is useful for:

- Testing UI components with realistic data
- Demonstrating trends and analytics features
- Developing without manual data entry
- CI/CD pipeline testing

## Running with Demo Data

### Option 1: Full Docker (All Services)

**Start everything in containers:**

```bash
DEMO_MODE=true VITE_DEMO_MODE=true docker-compose up -d --build
```

**Important:** Always use `--build` to ensure the frontend is built with `VITE_DEMO_MODE=true` baked in. Without it, the demo tab won't appear (Vite bakes env vars at build time).

Then visit `http://localhost:5173` and click the **Demo** tab, or log in with:

- **Email:** demo@example.com
- **Password:** demo@example.com

**Stop everything:**

```bash
docker-compose down
```

**Full reset (clears database):**

```bash
docker-compose down -v
DEMO_MODE=true VITE_DEMO_MODE=true docker-compose up -d --build
```

### Option 2: Local Development (Frontend + Backend locally, Database in Docker)

**Terminal 1 - Start database only:**

```bash
docker compose -f ./infra/docker-compose.yml --env-file ./.env up -d
```

**Terminal 2 - Start backend:**

```bash
cd backend
./mvnw -q -DskipTests compile
DEMO_MODE=true ./mvnw spring-boot:run
```

**Terminal 3 - Start frontend:**

```bash
cd frontend
VITE_DEMO_MODE=true npm run dev
```

Then visit `http://localhost:5173`

**Cleanup:**

```bash
docker compose -f ./infra/docker-compose.yml --env-file ./.env down
```

## What's in the Demo Account

The demo account comes pre-seeded with realistic health tracking data:

| Field               | Value                                            |
| ------------------- | ------------------------------------------------ |
| **Email**           | demo@example.com                                 |
| **Password**        | demo@example.com                                 |
| **Height**          | 175 cm                                           |
| **Active Goal**     | Weight Loss (target: 75 kg)                      |
| **Weight Data**     | 30 days of daily measurements                    |
| **Starting Weight** | ~82.5 kg                                         |
| **Trend**           | Realistic downward trend with daily fluctuations |

The 30-day weight data simulates a user consistently losing weight with natural daily variations, perfect for demonstrating:

- Weight trend charts
- Goal progress tracking
- Insights and recommendations
- Dashboard summaries

## Demo User ID

For reference in development/debugging:

```
UUID: 00000000-0000-0000-0000-000000000001
Email: demo@example.com
```

## Implementation Details

### Backend

- **Component:** `com.erikallas.ndl.data.DemoDataInitializer`
- **Activated by:** `demo.mode=true` in application.yaml
- **Idempotency:** Checks if demo user exists before creating, safe to restart
- **Data Creation:** Transactional batch insert of:
  - User entity
  - Health profile (175cm, moderate activity)
  - Weight loss goal
  - 30 days of weight entries

### Frontend

- **Config:** `VITE_DEMO_MODE` environment variable
- **UI Changes:** Shows demo banner and tab in LoginModal
- **Auto-Navigation:** Demo tab becomes default when demo mode is enabled
- **Visual Indicator:** Purple "Demo Mode Enabled" banner

### Configuration

`application.yaml`:

```yaml
demo:
  mode: ${DEMO_MODE:false} # Defaults to false if not set
```

## Development Workflow

### Quick Local Dev Setup (Recommended for Development)

```bash
# Terminal 1: Start database
docker compose -f ./infra/docker-compose.yml --env-file ./.env up -d

# Terminal 2: Start backend with demo data
cd backend && DEMO_MODE=true ./mvnw spring-boot:run

# Terminal 3: Start frontend with demo mode
cd frontend && VITE_DEMO_MODE=true npm run dev
```

### Quick Full Docker Setup (No Local Dependencies)

```bash
DEMO_MODE=true VITE_DEMO_MODE=true docker-compose up -d
```

### Reset Demo Data

**Local dev:**

```bash
docker compose -f ./infra/docker-compose.yml --env-file ./.env down -v
docker compose -f ./infra/docker-compose.yml --env-file ./.env up -d
```

**Full Docker:**

```bash
docker-compose down -v
DEMO_MODE=true VITE_DEMO_MODE=true docker-compose up -d
```

## Notes

- Demo data is only created if the specific demo user ID doesn't exist in the database
- Safe to run multiple times without duplicates
- Demo user has `email_verified = true`
- No Auth0 authorization needed for demo account (backend treats it specially)
- Perfect seed weights: Daily variance of ±0.3kg around overall downward trend

## Troubleshooting

**Demo tab not showing:**

- Ensure `VITE_DEMO_MODE=true` is set when running docker-compose
- **IMPORTANT:** Use `--build` flag: `DEMO_MODE=true VITE_DEMO_MODE=true docker-compose up -d --build`
- Vite bakes env vars at build time, so without rebuild they won't be in the app
- Check browser console (F12): should see log "🎬 LoginModal mounted - DEMO_MODE= true"
- Check container build: `docker logs ndl_frontend` should show build output

**Demo user not seeding / Already exists error:**

- If demo user already exists, the initializer skips silently (idempotent)
- This is fine - the demo tab will still show if `VITE_DEMO_MODE=true`
- To force recreate demo user: `docker-compose down -v` then `up -d --build`
- The `-v` flag removes database volumes

**Login fails with demo credentials:**

- Backend needs to support email/password auth (not just OAuth)
- Ensure `EmailPasswordAuthService` is configured
- Check backend logs: `docker logs ndl_backend | grep -i auth`
