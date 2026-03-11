# CAL Implementation Plan

Practical implementation roadmap for the calorie/meal-planning extension, aligned to `assignment-docs/CAL_ASSIGNMENT.md` and `assignment-docs/CAL_ASSIGNMENT_TEST.md`.

## 1. Scope and Principles

1. Build **mandatory requirements first**, extras after baseline is stable.
2. Keep AI deterministic where possible:
   - AI for planning/generation.
   - Backend function calls for nutrition math/validation.
3. Keep user-facing flows resilient:
   - strict JSON schema validation,
   - graceful fallbacks on AI errors,
   - clear actionable error messages.
4. Use ISO 8601 everywhere and store timestamps in UTC; render by user timezone.

## 2. Architecture (Target)

1. `dietary-preferences` module
   - Dietary preferences, allergies/intolerances, dislikes, cuisines, calorie/macros, meal frequency/timing, timezone.
   - Reuse data from Project 1 to prefill.
2. `recipe-catalog` module
   - Ingredients + recipes + recipe steps + recipe ingredients.
   - 500+ recipes and 500+ ingredients seed pipeline.
3. `recipe-rag` module
   - Recipe/ingredient embeddings + relevance retrieval (pgvector).
4. `meal-planner` module
   - Daily/weekly meal plan generation pipeline with sequential prompting (3+ stages).
   - Swap/regenerate/manual add/reorder.
   - Version history + restore.
5. `nutrition` module
   - Function-calling adapter.
   - Deterministic nutrition calculator from ingredient data.
6. `shopping-list` module
   - Shopping list generation, 5+ categories, quantity edits, exclusions.
7. `calorie-tracking` module
   - Intake logs, daily/weekly comparisons, trend lines (weekly/monthly).
8. `nutrition-insights` module
   - AI summaries + suggestions + fallback/caching.

## 3. Data Model (Initial Draft)

## 3.1 Preferences
- `nutrition_preferences`
  - `user_id` (PK/FK users)
  - `timezone` (IANA, e.g. `Europe/Tallinn`)
  - `calorie_target_kcal`
  - `protein_target_g`, `carbs_target_g`, `fats_target_g`
  - `meals_per_day`, `snacks_per_day`
  - `meal_times_json` (ISO 8601 local date-time with offset or local time + timezone)
  - timestamps
- `user_dietary_preferences` (many-to-many tags, >=15 supported values)
- `user_allergies` (many-to-many tags, >=10 supported values)
- `user_disliked_ingredients` (free text or ingredient FK)
- `user_cuisine_preferences` (tag list)

## 3.2 Catalog + Nutrition
- `ingredients`
  - `id`, `name`, `category`, `default_unit` (`g`/`ml`/piece), timestamps
- `ingredient_nutrition`
  - `ingredient_id` FK
  - normalized base quantity (100g/ml)
  - `calories_kcal`, `protein_g`, `carbs_g`, `fats_g`
  - optional micros later
- `recipes`
  - required assignment fields: `id`, `title`, `cuisine`, `meal`, `servings`, `summary`, `time_minutes`, `difficulty_level`, `dietary_tags`, `source`, `img`
- `recipe_ingredients`
  - `recipe_id`, `ingredient_id`, `quantity`, `unit` (normalized)
- `recipe_steps`
  - `recipe_id`, `step_number`, `step`, `description`, `ingredient_ids_json`
- `recipe_embeddings`
  - `recipe_id`, `embedding vector`, `embedding_model`, `updated_at`

## 3.3 Planner + Versioning
- `meal_plans`
  - `id`, `user_id`, `period_type` (`daily`/`weekly`), `start_date`, `end_date`, `status`
- `meal_plan_versions`
  - immutable snapshot: `id`, `meal_plan_id`, `version_number`, `source` (`ai`/`manual`), `payload_json`, `created_at`
- `meal_plan_meals`
  - `id`, `meal_plan_id`, `day_date`, `meal_type`, `planned_at`, `recipe_id` (nullable for custom), `custom_meal_json`, `position_index`
- `meal_alternatives`
  - `meal_plan_meal_id`, `recipe_id`, `rank`

## 3.4 Shopping + Tracking
- `shopping_lists`
  - `id`, `user_id`, `meal_plan_id` nullable, `generated_from` (`meal`/`plan`)
- `shopping_list_items`
  - `id`, `shopping_list_id`, `ingredient_id` nullable, `label`, `category`, `quantity`, `unit`, `excluded`
- `meal_intake_logs`
  - `id`, `user_id`, `meal_plan_meal_id` nullable, `consumed_at` ISO 8601, `recipe_snapshot_json`, `nutrition_snapshot_json`
- `nutrition_daily_summary`
  - `user_id`, `day_date`, totals + deficit/surplus
- `nutrition_weekly_summary`
  - `user_id`, `week_start`, totals + deficit/surplus

## 4. API Surface (MVP)

1. Preferences
   - `GET /api/nutrition/preferences`
   - `PUT /api/nutrition/preferences`
   - `GET /api/nutrition/preferences/options` (15+ diets, 10+ allergies)
2. Catalog
   - `GET /api/recipes/search?q=...`
   - `POST /api/recipes/filter`
   - `GET /api/recipes/{id}`
   - `POST /api/recipes/{id}/substitute-ingredient`
   - `POST /api/recipes/{id}/scale-servings`
3. Planner
   - `POST /api/meal-plans/generate` (daily/weekly)
   - `POST /api/meal-plans/{id}/regenerate`
   - `POST /api/meal-plans/{id}/meals/{mealId}/regenerate`
   - `POST /api/meal-plans/{id}/meals/{mealId}/swap`
   - `POST /api/meal-plans/{id}/reorder`
   - `POST /api/meal-plans/{id}/meals/manual`
   - `GET /api/meal-plans/{id}`
   - `GET /api/meal-plans/{id}/versions`
   - `POST /api/meal-plans/{id}/versions/{versionId}/restore`
4. Shopping
   - `POST /api/shopping-lists/from-plan/{mealPlanId}`
   - `POST /api/shopping-lists/from-meal/{mealId}`
   - `PATCH /api/shopping-lists/{id}/items/{itemId}`
5. Tracking + analytics
   - `POST /api/nutrition/intake`
   - `GET /api/nutrition/summary/daily?date=...`
   - `GET /api/nutrition/summary/weekly?weekStart=...`
   - `GET /api/nutrition/trends?range=weekly|monthly`
6. AI insights
   - `POST /api/nutrition/insights/generate`
   - `GET /api/nutrition/insights/latest`

## 5. AI Pipeline Design

## 5.1 Sequential prompting (required >=3 steps)
1. Strategy step
   - Input: user preferences/goals/history.
   - Output: daily macro split + meal timing strategy.
2. Structure step
   - Input: strategy + period (daily/weekly).
   - Output: meal skeleton per day (meal types + macro budget per slot).
3. Meal selection/generation step
   - Input: skeleton + retrieved recipes.
   - Output: concrete meals + alternatives.
4. Nutrition validation/refinement (optional but recommended)
   - Function-call nutrition totals and adjust plan to reduce macro/calorie drift.

## 5.2 Function calling (required)
- Tool contract examples:
  - `calculate_recipe_nutrition(ingredients, servings)`
  - `scale_ingredients(ingredients, from_servings, to_servings)`
  - `summarize_day_nutrition(meals[])`
- Required error handling:
  - parse/schema errors,
  - missing/invalid params,
  - execution/timeout/network errors,
  - rate limits.

## 5.3 RAG (required)
1. Seed recipe/ingredient datasets (>=500 each).
2. Generate embeddings (batch job).
3. Retrieve top-k by user query + constraints.
4. Augment final planning prompt with compact retrieved snippets.
5. Validate that output references compatible meals and restrictions.

## 6. Frontend Plan (MVP)

1. `NutritionPreferencesPage`
   - Prefill from existing health profile, confirm/edit only.
2. `MealPlannerPage`
   - Generate daily/weekly, swap/regenerate/reorder/manual add.
3. `RecipeExplorerPage`
   - Search/filter + recipe detail + substitution + serving scaler.
4. `ShoppingListPage`
   - Auto-categorized list + edits + exclusions.
5. `NutritionDashboardSection`
   - Daily progress bar (color-coded deficit/surplus),
   - macro split chart,
   - weekly/monthly trend lines,
   - AI nutrition summary cards.

## 7. Delivery Phases (Small Chunks)

## Phase 1: Foundations
1. Migrations + entities for preferences/catalog/planner core.
2. Seed scripts for diets/allergies enums and recipe/ingredient datasets.
3. Basic preferences API + UI with Project 1 prefill.

## Phase 2: Recipe + Nutrition Core
1. Recipe search/filter/detail endpoints.
2. Deterministic nutrition engine and serving scaler.
3. Function-calling wrapper + schema validator + error mapper.

## Phase 3: Meal Planning Core
1. Sequential prompting pipeline (3 steps minimum).
2. Meal plan CRUD, swap/reorder/manual add, regenerate.
3. Version snapshots and restore flow.

## Phase 4: RAG + Shopping + Tracking
1. Embeddings + retrieval service.
2. Shopping list generation/category/edit/exclude.
3. Intake logging + daily/weekly summaries + trend APIs.

## Phase 5: UX and Reliability
1. Dashboard visualizations and insight cards.
2. AI fallback/retry/caching.
3. Validation and error UX polish.

## 8. Acceptance Criteria Mapping (Mandatory)

Use this checklist when closing stories:

1. 15+ dietary prefs and 10+ allergies: options endpoint + DB seed + UI selectors.
2. Project 1 reuse: prefilled preferences from existing profile/goals.
3. ISO 8601 + timezone: DTO validation + DB + rendering tests.
4. Daily/weekly planner: both periods supported.
5. Meal customization: structure count, swap, reorder, manual add, regenerate.
6. Meal info completeness: name/type/nutrition per meal.
7. Shopping list: generation + 5 categories + quantity/exclude edits.
8. Sequential prompts: auditable pipeline logs with 3+ dependent steps.
9. RAG: embeddings + relevance retrieval + augmented prompts.
10. 500/500 dataset: reproducible seed/import script and counts.
11. Function calling for nutrition: calculations only via tools, with full error handling.
12. Recipe search/filter/details/substitution/portion scaling.
13. Intake tracking + daily/weekly deficit/surplus + visual trends.
14. AI summaries/suggestions integrated with dashboard/progress sections.
15. Content versioning with restore for meal plans.
16. API failure recovery: retry/backoff/fallback + user-friendly messages.

## 9. Non-Functional Standards

1. Security
   - validate all AI output with JSON schema.
   - authz all user-scoped resources by `user_id`.
2. Observability
   - structured logs per generation request (with correlation ID).
   - metrics: latency, error rate, fallback rate, token usage.
3. Performance
   - cache retrieval results and nutrition computations where safe.
4. Testing
   - integration tests for planner pipeline and function-calling errors.
   - component/e2e smoke tests for planner + shopping + tracking flows.

## 10. Recommended First Sprint (1 week)

1. Create migrations/entities for:
   - `nutrition_preferences`, `ingredients`, `ingredient_nutrition`, `recipes`, `recipe_ingredients`, `recipe_steps`.
2. Implement:
   - preferences options + get/update endpoints,
   - prefill from Project 1 profile/goals,
   - recipe search/filter/detail endpoints.
3. Add minimal frontend:
   - preferences form,
   - basic recipe explorer.
4. Seed at least 500 ingredients + 500 recipes (can be generated, then validated).

This gives a stable base before AI orchestration complexity.
