Strategy for Detecting and Handling AI Hallucinations
1. Strict Schema Validation (AiInsightService.java:170-191)

Enforce exact JSON structure with required fields: recommendations, reflection_question, summary
Validate array size (exactly 3 recommendations)
Reject invalid responses immediately
2. Content-Level Validation

Length limits: Recommendations max 220 chars, summary max 400 chars
Blank checks: Reject empty or whitespace-only content
Type validation: Ensure all fields are text, not nulls or objects
3. Prompt Engineering Safeguards (line 97-103)

"Keep recommendations concise, actionable, and safe"
"Avoid medical claims"
"You must respond ONLY with valid JSON"

Constrains the model from generating dangerous health advice.

4. Multi-Layer Fallback (line 118-133)

First: Try OpenAI
Second: Return last cached valid insight
Third: Return hardcoded safe fallback

Never show raw AI output that failed validation.

5. Input Hashing & Caching (line 76-82)

Cache AI responses for 24 hours
Same user state = same cached insight
Reduces hallucination risk by limiting AI calls
What You Can't Catch:

Plausible but incorrect health advice (e.g., "do 500 pushups daily")
The model saying something safe but useless
Why This Works:

You're not giving medical diagnoses, just motivational wellness tips
Validation ensures consistent format
Fallbacks prevent broken UX from bad AI output
Prompt constraints reduce likelihood of harmful advice