-- V21__nutrition_foundation.sql
-- Nutrition foundation for calorie counting / meal planning extension.
-- Adds preference storage, controlled taxonomy options, and recipe/ingredient catalog tables.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================
-- Nutrition preferences
-- =========================
CREATE TABLE IF NOT EXISTS nutrition_preferences (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    timezone TEXT NOT NULL DEFAULT 'UTC',
    calorie_target_kcal INTEGER CHECK (calorie_target_kcal IS NULL OR calorie_target_kcal > 0),
    protein_target_g NUMERIC(8, 2) CHECK (protein_target_g IS NULL OR protein_target_g >= 0),
    carbs_target_g NUMERIC(8, 2) CHECK (carbs_target_g IS NULL OR carbs_target_g >= 0),
    fats_target_g NUMERIC(8, 2) CHECK (fats_target_g IS NULL OR fats_target_g >= 0),
    meals_per_day SMALLINT NOT NULL DEFAULT 3 CHECK (meals_per_day BETWEEN 1 AND 8),
    snacks_per_day SMALLINT NOT NULL DEFAULT 0 CHECK (snacks_per_day BETWEEN 0 AND 6),
    meal_times_json JSONB NOT NULL DEFAULT '{}'::JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dietary_preference_options (
    code TEXT PRIMARY KEY,
    label TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS allergy_options (
    code TEXT PRIMARY KEY,
    label TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS user_dietary_preferences (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    preference_code TEXT NOT NULL REFERENCES dietary_preference_options(code),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, preference_code)
);

CREATE TABLE IF NOT EXISTS user_allergies (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    allergy_code TEXT NOT NULL REFERENCES allergy_options(code),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, allergy_code)
);

CREATE TABLE IF NOT EXISTS user_disliked_ingredients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ingredient_label TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_disliked_ingredients_user_id
    ON user_disliked_ingredients(user_id);

CREATE TABLE IF NOT EXISTS user_cuisine_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    cuisine_label TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_cuisine_preferences_user_id
    ON user_cuisine_preferences(user_id);

-- =========================
-- Recipe + ingredient catalog
-- =========================
CREATE TABLE IF NOT EXISTS ingredients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    default_unit TEXT NOT NULL CHECK (default_unit IN ('g', 'ml', 'piece')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_ingredients_name_lower
    ON ingredients ((lower(name)));

CREATE TABLE IF NOT EXISTS ingredient_nutrition (
    ingredient_id UUID PRIMARY KEY REFERENCES ingredients(id) ON DELETE CASCADE,
    base_quantity NUMERIC(8, 2) NOT NULL DEFAULT 100 CHECK (base_quantity > 0),
    base_unit TEXT NOT NULL CHECK (base_unit IN ('g', 'ml')),
    calories_kcal NUMERIC(8, 2) NOT NULL CHECK (calories_kcal >= 0),
    carbs_g NUMERIC(8, 2) NOT NULL CHECK (carbs_g >= 0),
    protein_g NUMERIC(8, 2) NOT NULL CHECK (protein_g >= 0),
    fats_g NUMERIC(8, 2) NOT NULL CHECK (fats_g >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS recipes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_id TEXT,
    title TEXT NOT NULL,
    cuisine TEXT,
    meal TEXT NOT NULL,
    servings INTEGER NOT NULL CHECK (servings > 0),
    summary TEXT,
    time_minutes INTEGER NOT NULL CHECK (time_minutes >= 0),
    difficulty_level TEXT NOT NULL,
    dietary_tags TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
    source TEXT NOT NULL,
    img TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_recipes_source_external_id
    ON recipes(source, external_id)
    WHERE external_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_recipes_cuisine ON recipes(cuisine);
CREATE INDEX IF NOT EXISTS idx_recipes_meal ON recipes(meal);

CREATE TABLE IF NOT EXISTS recipe_ingredients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipe_id UUID NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    ingredient_id UUID REFERENCES ingredients(id) ON DELETE SET NULL,
    ingredient_name TEXT NOT NULL,
    quantity NUMERIC(10, 2) NOT NULL CHECK (quantity > 0),
    unit TEXT NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_recipe_ingredients_recipe_id
    ON recipe_ingredients(recipe_id);

CREATE TABLE IF NOT EXISTS recipe_steps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipe_id UUID NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    step_number INTEGER NOT NULL CHECK (step_number > 0),
    step TEXT NOT NULL,
    description TEXT NOT NULL,
    ingredient_refs TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (recipe_id, step_number)
);

-- =========================
-- Controlled options seed
-- =========================
INSERT INTO dietary_preference_options (code, label)
VALUES
    ('vegetarian', 'Vegetarian'),
    ('vegan', 'Vegan'),
    ('pescatarian', 'Pescatarian'),
    ('keto', 'Keto'),
    ('low_carb', 'Low Carb'),
    ('high_protein', 'High Protein'),
    ('mediterranean', 'Mediterranean'),
    ('paleo', 'Paleo'),
    ('gluten_free', 'Gluten-Free'),
    ('dairy_free', 'Dairy-Free'),
    ('halal', 'Halal'),
    ('kosher', 'Kosher'),
    ('whole_food', 'Whole Food'),
    ('dash', 'DASH'),
    ('flexitarian', 'Flexitarian')
ON CONFLICT (code) DO NOTHING;

INSERT INTO allergy_options (code, label)
VALUES
    ('peanut', 'Peanut'),
    ('tree_nut', 'Tree Nut'),
    ('milk', 'Milk'),
    ('egg', 'Egg'),
    ('soy', 'Soy'),
    ('wheat', 'Wheat'),
    ('fish', 'Fish'),
    ('shellfish', 'Shellfish'),
    ('sesame', 'Sesame'),
    ('lactose_intolerance', 'Lactose Intolerance')
ON CONFLICT (code) DO NOTHING;
