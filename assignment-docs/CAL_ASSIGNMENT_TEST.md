### Testing

Ensures that software works as expected by validating features against requirements. It helps catch bugs early, improves reliability, and maintains high-quality standards in development.

How to do testing?

1. 1. Clone the repository, then build and run the submitted code.
2. 2. Agree on your teamwork: how do you divide testing between reviewers?
3. 3. Test functionality and check compliance with the requirements.
4. 4. Provide feedback in the group chat and request fixes if necessary.
5. 5. Clearly state what changes are mandatory and what are optional fixes.
6. 6. Repeat the testing cycle after submitters make the requested changes for as many times as is needed.

#### Mandatory

### The README file contains a clear project overview, setup instructions, and usage guide.

Documentation includes:

* Overview of prompt engineering strategy
* AI model selection rationale
* Data model decisions
* Error handling methods used in the project.

### Nutritional planning functionality supports at least 15 different dietary preferences and 10 food allergies/intolerances.

### The platform collects and utilizes user preferences:

Dietary preferences, allergies and intolerances, disliked ingredients, cuisine preferences, calorie and macronutrient targets, meal frequency and timing

### System reuses information from Project 1 without requiring duplicate input.

Input fields are pre-filled for information that has already been provided. Only confirmation should be asked.

### All dates and times follow the ISO 8601 standard format.

### System generates complete daily and weekly meal plans based on user preferences.

Both daily and weekly meal plan durations are supported.

### System allows customizing meal structures.

e.g. number of meals and snacks per day

### Each meal has basic information.

Name, meal type (e.g. breakfast/lunch/dinner/snack or similar), nutritional value.

### System provides alternative meal options and allows users to select replacements.

### System allows reordering meals within a day or swapping meals between different days.

It is possible to change dinner to lunch and Mondays dinner for Tuesdays dinner.

### System allows users to manually add custom meals.

### System allows regenerating individual meals or entire meal plans while preserving preferences.

### Shopping list can be generated from a specific meal or meal plan.

### Shopping list items are automatically categorized by food group to at least 5 meaningful categories.

e.g. cheese and milk are categorized under dairy.

### Shopping list allows both quantity adjustments and item exclusions.

Users can modify amounts and completely remove ingredients from generated shopping lists.

### Sequential prompting with at least 3 distinct steps is used for meal plan generation.

Verify that at least 3 distinct prompts are used in sequence and each next prompt uses results from previous step. The student can explain and conceptualize the logical progression of each step and how breaking down the process improves the quality of generated meal plans compared to a single-prompt approach.

### Retrieval-Augmented Generation is used to generate recipes and nutritional values.

The student can explain the function of all the components in their RAG implementation, their importance in the RAG pipeline (database -> embedding -> retrieval -> augmentation -> generation) and how RAG improves the quality of generated recipes compared to generation from scratch.

### Recipe and ingredient databases have at least 500 entries each.

### RAG implementation includes (at least rudimentary) vector embeddings and relevance-based search.

Recipe and ingredient databases utilize vector similarity for retrieval.

### Response to the user is generated from an augmented prompt.

### System uses clearly defined functions to provide accurate nutritional calculations.

All nutritional calculations are done by function calling.

### Function calling implementation includes proper error handling.

Error handling covers parsing errors, missing parameters, invalid values, execution errors, timeout errors, rate limits and connectivity issues.

### Recipes can be searched by name, ingredients, or cuisine.

### Recipes can be filtered.

Filters include dietary restrictions, allergies, ingredients, calorie and macronutrient amounts, and preparation time.

### Recipes display ingredients, step-by-step instructions and nutritional information summarized from used ingredients.

Nutritional information is also visualized (e.g. chart, graph)

### Variety of custom recipes can be generated with AI based on user preferences.

Generated recipes are varied, include instructions and nutritional information.

### Ingredients in a recipe can be substituted with AI.

Option to base alternatives on food item availability and user preferences.

### Portions can be adjusted by modifying serving size.

Ingredient quantity is automatically recalculated. Function calling is used for meal nutritional value recalculation.

### All measurements are standardized to common units in data structures.

Solids are in grams, liquids in milliliters, energy in kilocalories and time in minutes.

### Recipe data structure has all required fields.

Verify that the recipe data structure includes all of these required fields:

* id
* title
* cuisine
* meal
* servings
* ingredients (array with id, name, quantity)
* summary
* time
* difficulty_level
* dietary_tags
* source
* img
* preparation (array with step, description, ingredients)

### Ingredient data structure has all required fields.

Verify that the ingredient data structure includes all of these required fields:

* id
* label
* unit
* quantity
* nutrition (object containing):
  * calories
  * carbs
  * protein
  * fats

### Nutritional analysis includes calorie (kcal) and macronutrient (grams and % of daily target) tracking per meal/day.

Macronutrient breakdown is meaningfully visualized (e.g. graph, chart).

### Nutritional analysis tracks users nutritional intake and compares it to nutritional intake goals.

Comparisons are available on daily and weekly basis with accurate deficit/surplus calculations.

### Nutritional intake tracking is visualized informatively and intuitively.

Progress towards calorie targets is visualized with a progress bar and is color-coded to indicate caloric surplus or deficit.

### Nutritional intake tracking includes visualized trend lines

Trend line visualizes daily caloric deficit/surplus on a weekly and monthly basis.

### AI-driven nutritional analysis offers regular summaries.

Summaries include key achievements in terms of users goals, potential concerns and macronutrient balance analysis.

### AI-driven nutritional analysis provides users improvement suggestions

Improvement suggestions include food recommendations, meal timing adjustments, portion size modifications, alternative ingredients and possible meal plan optimizations.

### Nutritional analysis features are seamlessly integrated.

Daily tracking is available on dashboard. Historical analysis is available in the Progress Charts section. AI-driven nutritional insights are grouped together with other insights.

### Meal planner uses relevant user data to improve performance and personalization.

BMI and weight goals are used to calculate calorie targets. Activity level is used to adjust portions and macros Wellness score is updated based on nutrition.

### Student can explain and justify their AI model choices for recipe generation vs. nutritional analysis

### Few-shot examples are used in prompts.

Student can explain their example selection strategy.

### Parameter adjustments are used for AI-generated outputs.

Student can explain temperature and top-p usage decisions.

### System handles API errors gracefully with clear user-friendly error messages.

Rate limits, timeouts, and malformed responses are handled with meaningful user feedback.

### System implements recovery mechanisms for failed AI requests.

At least one fallback strategy should be present (caching, retry logic, alternative models, etc.).

### Content versioning allows restoring previous meal plans.

User can access and restore earlier meal plan versions.

#### Extra

### Project runs entirely through Docker with a single command

The project includes a Dockerfile or script that builds and runs the app with one command. Docker is the only requirement, no manual setup or dependency installation is required.

### System maintains user preference history to inform future recommendations.

Tracks user decisions and feedback to improve personalization over time.

### Nutritional analysis includes micronutrients.

One point should be given for each implemented improvement in project "Micronutrients" section.

### System includes a mechanism for user feedback to improve RAG database quality and size.

One point should be given for each implemented improvement mechanism outlined in project "Community-driven RAG" section

### Nutritional data has been extended with additional fields (in addition to micronutrients).

Up to two points can be given for an enhanced data schema (recipe/meal plan). Award two points if the schemas are significantly improved.

### Student has implemented additional technologies, security enhancements and/or features beyond the core requirements.

### Overall implementation quality demonstrates exceptional attention to detail and thoroughness beyond minimum requirements.

Evaluate the following aspects of overall implementation quality:

* Code quality and organization
* Documentation completeness and clarity
* Consistency in error handling and user experience
* Testing coverage and quality
* Implementation of best practices throughout

Award full points for implementations that go significantly beyond minimum requirements and demonstrate professional-grade quality.
