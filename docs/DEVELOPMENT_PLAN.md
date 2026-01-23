# Development Plan - Numbers Don't Lie

**Goal**: Build solid Phase 1 & 2 foundation, then add new features

## Phase 1: Core Platform (Complete)

### ✅ Completed

- User registration with email verification
- Email-password and Auth0 authentication
- Password reset via email
- Input validation with error handling
- Basic health profile structure
- Database schema (Flyway migrations)
- Refactored folder structure (feature-based)
- All tests passing

### 🔴 CRITICAL BLOCKERS (Must Do Before Phase 2)

#### 1. Refresh Token Mechanism (Estimated: 2-3 hours)

**Why**: Current setup only has access tokens. Users lose session after token expires.

**What to build**:

- Add `refresh_token` column to `users` table (V5 migration)
- Create `RefreshToken` entity (store token, expiry, revocation)
- Implement `/api/auth/refresh` endpoint
- Update `AuthController` to issue both access + refresh tokens
- Update `AuthService` to validate/rotate refresh tokens

**Acceptance**:

- User gets access token (15 min) + refresh token (7 days) on login
- When access token expires, `/api/auth/refresh` with refresh token → new access token
- Refresh token expires after 7 days → must re-login
- Tests verify token expiry and refresh works

---

#### 2. Complete Health Profile Schema (Estimated: 2-3 hours)

**Why**: Can't collect data for AI without complete profile structure.

**What to build**:

- Extend `HealthProfileEntity` with missing fields:
  - `dietary_preferences` (JSONB array: vegetarian, vegan, keto, etc.)
  - `dietary_restrictions` (JSONB array: dairy_free, gluten_free, etc.)
  - `fitness_goals` (link to Goal entity - already exists)
  - `fitness_assessment` (JSONB object):
    ```json
    {
      "current_activity_frequency": 3, // 0-7 days/week
      "exercise_types": ["cardio", "strength"],
      "avg_session_duration": "30-60min",
      "fitness_level": "intermediate", // beginner/intermediate/advanced
      "preferred_environment": "gym", // home, gym, outdoors
      "exercise_time": "morning", // morning, afternoon, evening
      "endurance_level": "can run 30 min",
      "strength_level": "20 pushups"
    }
    ```
- Create migration V5 (or extend V4)
- Update `ProfileController` endpoints to accept/return all fields
- Add validation for each field

**Acceptance**:

- POST /api/profile → accepts all health profile fields
- GET /api/profile → returns complete profile with all fields
- All fields validated and stored in DB

---

#### 3. BMI Classification & Storage (Estimated: 1-2 hours)

**Why**: Wellness score needs BMI classification, must be calculated and stored.

**What to build**:

- Create `BMIClassification` enum: UNDERWEIGHT, NORMAL, OVERWEIGHT, OBESE
- Add `bmi_value` and `bmi_classification` columns to `HealthProfileEntity`
- Implement `BMICalculator` service
  - Formula: weight (kg) / height² (m²)
  - Classification logic
- Update `ProfileController.updateProfile()` to calculate on save
- Store both raw BMI value and classification for trend analysis

**Acceptance**:

- User updates height/weight → BMI calculated and stored
- GET /api/profile includes bmi_value and bmi_classification
- Tests verify calculation accuracy (e.g., 70kg, 1.75m = 22.86 = NORMAL)

---

### 🟡 HIGH PRIORITY (Phase 2 Foundation)

#### 4. Wellness Score Calculation (Estimated: 3-4 hours)

**Why**: Core metric that drives insights and progress visualization.

**What to build**:

- Create `WellnessScoreCalculator` service with formula:

  ```
  score = (bmi_score * 0.3) + (activity_score * 0.3) + (progress_score * 0.2) + (habits_score * 0.2)

  - bmi_score: 0-100 based on BMI classification
  - activity_score: 0-100 based on weekly activity frequency (0-7 days)
  - progress_score: 0-100 based on goal progress towards target
  - habits_score: 0-100 based on consistency (streak, frequency)
  ```

- Add `wellness_score` column to `HealthProfileEntity`
- Create `ScoreHistory` entity to track daily scores
- Add `/api/profile/wellness-score` endpoint
- Update score whenever any contributing metric changes

**Acceptance**:

- GET /api/profile/wellness-score returns current score (0-100)
- Score updates when user changes activity, weight, goal, etc.
- Historical scores queryable for trend analysis

---

#### 5. Progress Tracking & Goal Calculation (Estimated: 3-4 hours)

**Why**: Needed for showing user progress and calculating wellness score.

**What to build**:

- Extend `GoalEntity` with:
  - `target_value` (target weight, target activity, etc.)
  - `created_at`, `target_date`
  - `milestone_tracking` (JSONB for milestone achievements)
- Create `ProgressCalculator` service:
  - Distance to target
  - % progress towards goal
  - Estimated completion date
  - Milestones achieved (e.g., every 5% for weight)
- Add `/api/goals/:id/progress` endpoint
- Update progress whenever relevant metric changes

**Acceptance**:

- GET /api/goals/:id/progress returns:
  ```json
  {
    "target_value": 65,
    "current_value": 75,
    "progress_percentage": 50,
    "estimated_completion": "2026-06-23",
    "milestones_achieved": ["10% progress", "20% progress"],
    "days_remaining": 181
  }
  ```

---

#### 6. AI Context Preparation (Estimated: 2-3 hours)

**Why**: AI needs normalized, clean data + context to generate good insights.

**What to build**:

- Create `AIContextBuilder` service that:
  - Normalizes units (weight to kg, height to cm)
  - Removes PII (no names, emails to AI)
  - Structures data for AI prompt:
    ```json
    {
      "user_id": "anonymous-uuid",
      "metrics": {
        "current_bmi": 22.86,
        "current_weight": 70,
        "target_weight": 65,
        "activity_frequency": 4,
        "wellness_score": 72
      },
      "goals": ["lose 5kg", "increase to 5 workouts/week"],
      "preferences": ["vegetarian"],
      "restrictions": ["dairy_free"],
      "history": {
        "weight_trend": "stable",
        "activity_trend": "improving",
        "score_trend": "stable"
      }
    }
    ```
- Update `OpenAiClient` to use this structured context
- Test with real AI calls to verify quality

**Acceptance**:

- AI prompts include structured, normalized user context
- No PII sent to AI
- Insights are personalized and accurate

---

### 🟢 MEDIUM PRIORITY (Phase 2)

#### 7. Response Validation for Restrictions (Estimated: 1-2 hours)

**Why**: Ensure AI recommendations respect user dietary/health restrictions.

**What to build**:

- Validator that checks AI response against user restrictions
- Filter out recommendations that violate restrictions
- Log any hallucinations for monitoring
- Add `/api/insights/validate` endpoint for testing

**Acceptance**:

- Vegetarian user never gets meat-based recommendations
- Dairy-free user never gets dairy recommendations

---

#### 8. Insight Caching (Estimated: 2-3 hours)

**Why**: Reduce API costs, improve response time, graceful degradation when AI down.

**What to build**:

- Create `AIInsightCache` table with:
  - `user_id`, `insight_text`, `created_at`, `expires_at`
- `InsightCacheService` to store/retrieve
- When generating new insight:
  1. Try to call AI
  2. If AI fails, return cached insight
  3. If no cache, return generic insight
- Insights expire after 7 days (regenerate weekly)

**Acceptance**:

- AI insight cached when generated
- If AI down, system returns most recent cached insight
- Old insights replaced weekly

---

## Phase 3: Frontend Visualization

### 🟡 Core Dashboard (Estimated: 6-8 hours)

Build reactive dashboard showing:

1. BMI card (with classification color)
2. Wellness score gauge (0-100 with color gradient)
3. Goal progress section (progress bars with milestones)
4. Latest AI insight (highlighted, actionable)
5. Quick stats (activity level, weight change this week)

---

## Timeline Recommendation

**Assuming 2-3 hours coding per day:**

| Week | Focus            | Features                                     |
| ---- | ---------------- | -------------------------------------------- |
| 1    | Phase 1 Critical | Refresh tokens, complete profile schema, BMI |
| 2    | Phase 2 Backend  | Wellness score, goal progress, AI context    |
| 3    | Phase 2 Polish   | Response validation, caching, testing        |
| 4    | Phase 3 Frontend | Dashboard, charts, visualization             |

**Total: 4 weeks to complete core platform**

---

## Success Criteria (From Assignment Test)

### Must Have for Submission

- ✅ Registration + email verification
- ✅ Email-password + 2 OAuth auth
- ✅ Password reset
- ⏳ Refresh token mechanism
- ⏳ 2FA (optional)
- ✅ Protected routes require email verification
- ⏳ Access token expiry (+ refresh mechanism)
- ⏳ Data usage consent
- ⏳ Full health profile data collection
- ⏳ Data encryption (transit ✅, at rest ⏳)
- ⏳ Weight history tracking
- ⏳ Wellness score displayed
- ⏳ BMI + wellness + goal progress on dashboard
- ⏳ Weight chart with goal reference line
- ⏳ AI insights personalized to goals
- ⏳ AI excludes PII
- ⏳ Rate limiting
- ⏳ Mobile responsive charts
- ⏳ README with setup guide

**Current: 8/19 done (42%)**

---

## Architecture Decisions Made

### ✅ Feature-Based Organization

Each feature (auth, user, health/profile, health/weight, health/goal, health/summary) in its own package with api/, service/, model/.

### ✅ Centralized Error Handling

GlobalExceptionHandler catches exceptions, returns consistent ApiError responses.

### ✅ Dual Auth Strategy

Support both Auth0 and email-password in same system. Users can authenticate via either method.

### ✅ JWT Access + Refresh Tokens (TO IMPLEMENT)

Short-lived access tokens (15 min), long-lived refresh tokens (7 days) for better security.

### ✅ AI Context Builders

Normalize and structure data before AI to ensure quality.

### ✅ Caching for AI Responses

Reduce costs and ensure graceful degradation.

---

## Questions to Answer Before Building

1. **OAuth2 Provider 2**: Have Auth0, need 1 more (Google? Microsoft? GitHub?)
2. **2FA Implementation**: Optional, but how? Email codes? TOTP? Keep it simple?
3. **Data Encryption at Rest**: Enable PostgreSQL encryption or handle in app?
4. **AI Response Format**: Keep current JSON Schema approach or improve?
5. **Mobile Priority**: Build dashboard for mobile first, then desktop?

---

## Next Meeting Agenda

1. Confirm critical blocker priorities
2. Pick OAuth2 provider #2
3. Decide on 2FA approach
4. Set sprint schedule
5. Assign tasks if team involved
