# AI Coding Agent Instructions

## Project Overview

**Numbers Don't Lie** is a wellness application for tracking and improving health metrics (weight, goals, AI-generated insights). It uses a Spring Boot 4 backend with JWT/OAuth2 authentication (Auth0), React 19 frontend with Vite, PostgreSQL 16 database, and Docker Compose for infrastructure.

## Architecture Essentials

### Service Boundary & Communication

- **Backend** (`backend/src/main/java/com/erikallas/ndl/`): Spring Boot REST API on port 8080
  - Database layer: JPA entities in `user/`, migrations in `db/migration/` (Flyway)
  - API controllers in `api/` with Auth0 JWT validation
  - AI integration: `ai/openai/OpenAiClient.java` handles structured OpenAI API calls with JSON Schema validation
  - Audit system: `audit/` tracks user events to `audit_events` table (JSONB metadata)
  - **Key pattern**: Service + Repository classes provide business logic; controllers accept `JwtAuthenticationToken auth` from Spring Security
- **Frontend** (`frontend/src/`): React + Vite, TypeScript, Tailwind CSS on port 5173

  - Auth via Auth0 React SDK (`@auth0/auth0-react`), tokens passed to API calls
  - API client: `shared/api/client.ts` with `apiFetch()` function (typed, Bearer token injection)
  - **Key pattern**: `useAuthedQuery()` hook retrieves token and calls API; custom hooks for each domain (`useApiQuery`, `useAuthToken`)
  - Pages: Dashboard (summary), Trends (weight history), Goals (active goal tracking), CheckIn (record weight)
  - Navigation: Sticky header, bottom tab bar (AppShell.tsx)

- **Database** (PostgreSQL 16): Migrations create `audit_events`, `app_users`, user profile/weight/goal tables
  - **Location**: `backend/src/main/resources/db/migration/V*.sql`
  - Flyway auto-runs on startup; schema-first approach

### Critical Integration Points

1. **Auth0 OAuth2 JWT Flow**:

   - Frontend: `useAuthToken()` gets token from Auth0 context
   - Backend: `JwtAuthenticationToken` auto-injected into controller methods via `@SecurityRequirement(name = "bearerAuth")`
   - Audience validation: `https://numbers-dont-lie-api` (set in `AUTH0_AUDIENCE` env var)
   - **Key files**: `frontend/src/shared/auth/useAuthedQuery.ts`, `api/MeController.java`

2. **API Contract**:

   - Backend returns typed JSON; frontend defines `types.ts` interfaces for responses
   - Error handling: Backend wraps in `ApiError` with `message`, `fieldErrors`, `timestamp`
   - Frontend: `ApiError` class with `status`, `bodyJson`, `bodyText`; `explainApiError()` utility formats errors for UI

3. **AI Insight Generation**:
   - `OpenAiClient.generateInsightJson()` calls OpenAI v1 API with JSON Schema mode (gpt-4o-mini by default)
   - Response schema enforces 3 recommendations + reflection_question + summary (all max 220 chars)
   - Environment: `OPENAI_API_KEY`, `OPENAI_MODEL`
   - **Key method**: `generateInsightJson(systemPrompt, userPrompt)` returns JSON string

## Build & Execution

### Local Development

```bash
# Terminal 1: Start database
cd infra && docker-compose up

# Terminal 2: Backend (Maven wrapper)
cd backend && ./mvnw spring-boot:run

# Terminal 3: Frontend (Vite dev server)
cd frontend && npm install && npm run dev
```

**Config files**:

- Backend: `application-local.yaml` (local DB, Auth0 issuer)
- Frontend: `src/shared/config.ts` defines `API_BASE_URL`
- Env vars: `DATABASE_URL`, `OPENAI_API_KEY`, `AUTH0_AUDIENCE`, `SERVER_PORT`

### Testing

```bash
# Backend unit tests (uses Maven, runs ApplicationTests.java)
cd backend && ./mvnw test

# Test endpoints manually
curl -s http://localhost:8080/api/ping                      # health check
curl -s http://localhost:8080/actuator/health               # Spring Actuator
curl -s -X POST http://localhost:8080/api/audit-test        # audit logging
```

**Swagger/OpenAPI**: Available at `http://localhost:8080/swagger-ui.html` (requires `/api/me` to inspect JWT claims)

## Code Patterns & Conventions

### Backend (Java/Spring Boot 4)

- **Entity layer**: JPA `@Entity` classes with UUID primary keys, `OffsetDateTime` for timestamps
  - Example: `AppUserEntity` with fields `id` (UUID), `auth0Sub`, `email`, `createdAt`
- **Service pattern**: Single-class services with `@Transactional` for DB operations

  ```java
  // UserService.java
  @Service
  public class UserService {
    public AppUserEntity ensureUser(String auth0Sub, String emailOrNull) {
      return repo.findByAuth0Sub(auth0Sub)
        .orElseGet(() -> repo.save(new AppUserEntity(...)));
    }
  }
  ```

- **Controller pattern**: Methods accept `JwtAuthenticationToken auth`, extract claims via `auth.getToken()`

  ```java
  @SecurityRequirement(name = "bearerAuth")
  @RestController
  public class MeController {
    @GetMapping("/api/me")
    public Map<String, Object> me(JwtAuthenticationToken auth) {
      var jwt = auth.getToken();
      return Map.of("sub", jwt.getSubject(), "email", jwt.getClaimAsString("email"));
    }
  }
  ```

- **Error handling**: `GlobalExceptionHandler` catches exceptions, returns `ApiError` with `fieldErrors` array for validation

### Frontend (React/TypeScript)

- **Hook-based data fetching**: Custom `useApiQuery()` for async state, `useAuthedQuery()` wraps with token injection

  ```typescript
  function getProfile(token: string): Promise<Profile> {
    return api.get<Profile>("/api/profile", token);
  }
  const profileQ = useAuthedQuery(getProfile);
  // Use: profileQ.data, profileQ.loading, profileQ.error
  ```

- **API client**: `apiFetch()` utility handles headers, Auth Bearer token, error parsing

  - All requests include `Content-Type: application/json` automatically
  - Typed responses: `api.get<T>()`, `api.post<T>()`, `api.put<T>()`, `api.del<T>()`

- **Component structure**: Shared UI (Button, Card, Alert, Spinner, SelectField, TextField) in `shared/ui/`

  - Page components compose multiple `useAuthedQuery` hooks, render loading/error states consistently
  - Example: Dashboard loads 5 concurrent queries (me, profile, goal, weights, insights)

- **Tailwind CSS**: All styling via utility classes; `max-w-md` for mobile-first layouts; bottom navigation uses `fixed bottom-0` pattern

### Database Migrations

- Files: `V1__init.sql`, `V2__users_profile_weight.sql`, `V3__goals_and_ai_insights.sql`
- Flyway auto-discovers and runs in sequence on Spring Boot startup
- Use Postgres-specific features: UUID extension (`pgcrypto`), JSONB columns, timestamptz

## Development Workflow

1. **Adding API endpoint**: Create service method → add Spring REST controller with `@SecurityRequirement` → define response DTO/type
2. **Adding database table**: Create `VN__description.sql` migration → create JPA entity + repository → expose via service
3. **Frontend page**: Create React component with `useAuthedQuery()` hooks → call new API endpoints → render with shared UI components
4. **Testing changes**: Manual curl for backend, reload Vite dev server for frontend, check `localhost:8080/swagger-ui.html`
5. **AI integration**: Modify `OpenAiClient.generateInsightJson()` schema/prompts, test with `OPENAI_API_KEY` set in `.env` or docker-compose

## Key Files Reference

| Component        | Key Files                                                                         |
| ---------------- | --------------------------------------------------------------------------------- |
| Backend Core     | `Application.java`, `api/MeController.java`, `user/UserService.java`              |
| Auth Integration | `config/SecurityConfig.java`, `api/GlobalExceptionHandler.java`                   |
| AI Integration   | `ai/openai/OpenAiClient.java`                                                     |
| Database         | `db/migration/V*.sql`, `user/AppUserEntity.java`                                  |
| Frontend Core    | `App.tsx`, `layout/AppShell.tsx`, `shared/api/client.ts`                          |
| Auth Hooks       | `shared/auth/useAuthToken.ts`, `shared/auth/useAuthedQuery.ts`                    |
| API Layer        | `api/me.ts`, `api/profile.ts`, `api/weight.ts`, `api/goals.ts`, `api/insights.ts` |

## Important Constraints & Trade-offs

- **No frontend build step in dev**: Vite handles TypeScript + JSX transpilation on-the-fly
- **JWT validation**: Backend relies entirely on Auth0 issuer URI; `ddl-auto: none` means schema must match migrations
- **Structured AI output**: OpenAI JSON Schema mode enforces response format; if schema changes, update both `OpenAiClient` schema map and frontend types
- **Flyway migrations**: Cannot be rolled back in production (append-only); test migrations locally before committing
- **Mobile-first**: Responsive design via Tailwind; no dedicated mobile app

## Debugging Tips

- Backend logs: Check Spring Boot console for SQL, migrations, auth failures
- Frontend: Browser DevTools Network tab shows API calls + Auth0 token in Authorization header
- Database: Connect with `psql -h localhost -U ndl -d ndl` (password: `ndl_password`)
- Auth0 tokens: Copy JWT from browser localStorage → paste into [jwt.io](https://jwt.io) to inspect claims
- OpenAI: Set `OPENAI_API_KEY=""` to disable; endpoint returns error instead of crashing
