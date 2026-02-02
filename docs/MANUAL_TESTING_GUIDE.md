# Manual Testing Guide - Soft Delete Implementation

## Quick Start

### Terminal 1: Start Database & Backend

```bash
cd /home/erik/dev/kood/erikallas/numbers-dont-lie/infra
docker-compose up

# In another terminal, start backend:
cd /home/erik/dev/kood/erikallas/numbers-dont-lie/backend
./mvnw spring-boot:run
```

### Terminal 2: Start Frontend

```bash
cd /home/erik/dev/kood/erikallas/numbers-dont-lie/frontend
npm run dev
```

### Terminal 3: Run Smoke Tests

```bash
chmod +x /home/erik/dev/kood/erikallas/numbers-dont-lie/scripts/smoke-test.sh
/home/erik/dev/kood/erikallas/numbers-dont-lie/scripts/smoke-test.sh
```

## Access Points

- **Frontend**: http://localhost:5173
- **Swagger API Docs**: http://localhost:8080/swagger-ui.html
- **Backend Health**: http://localhost:8080/api/ping

## Manual Testing Scenarios

### Scenario 1: Health Endpoint Test

**Objective**: Verify backend is running and health check works

1. Open terminal and run:
   ```bash
   curl -s http://localhost:8080/api/ping | jq .
   ```
2. **Expected result**: Response with status "ok" or similar

### Scenario 2: Create Health Profile

**Objective**: Test POST endpoint with input validation and response DTO

1. In Swagger UI (http://localhost:8080/swagger-ui.html), find **POST /api/profile**
2. Click "Try it out"
3. Enter JSON body:
   ```json
   {
     "height_cm": 180,
     "activity_level": "moderate",
     "weight_kg": 75
   }
   ```
4. **Expected result**:
   - Status: 201 Created
   - Response body contains `height_cm`, `activity_level`, `wellness_score`, `created_at`
   - Example: `{"height_cm":180,"activity_level":"moderate","bmi_value":23.15,"bmi_classification":"Normal weight","wellness_score":75,"created_at":"2026-02-02T18:30:00+02:00","updated_at":"2026-02-02T18:30:00+02:00"}`

### Scenario 3: Create Weight Entry

**Objective**: Test weight tracking with pagination

1. In Swagger UI, find **POST /api/weight**
2. Click "Try it out"
3. Enter JSON body:
   ```json
   {
     "weight_kg": 75.5,
     "measured_at": "2026-02-02T10:00:00+02:00",
     "note": "Morning weigh-in after exercise"
   }
   ```
4. **Expected result**:
   - Status: 201 Created
   - Response contains `id`, `weight_kg`, `measured_at`, `note` fields

5. Create 5 more weight entries with different dates to test pagination
   - Repeat POST with different `measured_at` times

### Scenario 4: Test Pagination

**Objective**: Verify pagination works correctly

1. In Swagger UI, find **GET /api/weight/history**
2. Add parameters: `page=0` and `size=3`
3. Click "Execute"
4. **Expected results**:
   ```json
   {
     "content": [ ... ],
     "pageNumber": 0,
     "pageSize": 3,
     "totalElements": 6,
     "totalPages": 2,
     "isFirst": true,
     "isLast": false
   }
   ```
5. Change to `page=1` and verify:
   - `isFirst`: false
   - `isLast`: true
   - `content` has different entries

### Scenario 5: Update Weight Entry

**Objective**: Test PATCH endpoint and BMI recalculation

1. From Scenario 3, copy a weight entry `id` (e.g., "abc-def-ghi-jkl")
2. In Swagger UI, find **PATCH /api/weight/{id}**
3. Enter the ID in the path parameter
4. Enter JSON body:
   ```json
   {
     "weight_kg": 76.0,
     "measured_at": "2026-02-02T14:00:00+02:00",
     "note": "Afternoon weigh-in"
   }
   ```
5. **Expected result**:
   - Status: 200 OK
   - Response shows updated `weight_kg: 76.0`
   - Updated profile should reflect new BMI

6. **Verify BMI recalculation**:
   - Get profile via **GET /api/profile**
   - `bmi_value` should be recalculated based on new weight

### Scenario 6: Test Soft Delete

**Objective**: Verify soft delete works and records are filtered

1. Create 3 weight entries (if not already done)
2. Get list: **GET /api/weight** → note the count (e.g., 3)
3. Copy one entry `id`
4. Delete it: **DELETE /api/weight/{id}**
   - **Expected**: Status 204 No Content
5. Get list again: **GET /api/weight**
   - **Expected**: Count is now 2 (deleted entry not shown)
6. **Verify in database** (optional):
   ```bash
   psql -h localhost -U ndl -d ndl
   SELECT * FROM weight_entries WHERE id = 'abc-def-ghi-jkl';
   ```

   - Should show `deleted_at` is NOT NULL
   - All non-deleted entries have `deleted_at IS NULL`

### Scenario 7: Test Goal Creation & Active Status

**Objective**: Verify goal management with active status

1. In Swagger UI, find **POST /api/goals**
2. Enter JSON body:
   ```json
   {
     "goal_type": "weight_loss",
     "target_weight_kg": 70.0,
     "target_activity_days_per_week": 5,
     "notes": "Lose 5kg by end of quarter"
   }
   ```
3. **Expected result**:
   - Status: 201 Created
   - Response includes `id`, `is_active: true`, `created_at`

4. Test **GET /api/goals/active**
   - **Expected**: Returns the goal created above

5. Create another goal with `goal_type: "fitness"`
6. Test **GET /api/goals**
   - **Expected**: Returns all user's goals

### Scenario 8: Test Ownership Validation

**Objective**: Verify users can only access their own resources

**Note**: Requires two different Auth0 tokens or mock JWT claims

1. **Without a valid token**:
   - Remove Authorization header from request
   - Try **GET /api/weight**
   - **Expected**: Status 401 Unauthorized

2. **With token from different user** (if available):
   - Get User A's weight entry ID
   - Use User B's token
   - Try **GET /api/weight/{id}**
   - **Expected**: Status 404 Not Found or 403 Forbidden

3. **Browser-based test**:
   - Open Frontend at http://localhost:5173
   - Login via Auth0
   - Create a weight entry
   - Open DevTools Network tab
   - Check Authorization header contains Bearer token
   - Verify no errors in Console

### Scenario 9: Test Goal Progress Tracking

**Objective**: Verify goal progress recording

1. Create a goal (from Scenario 7, copy goal `id`)
2. In Swagger UI, find **POST /api/goals/{id}/progress/record**
3. Enter goal ID in path
4. Enter JSON body:
   ```json
   {
     "progress_value": 5.0,
     "note": "Completed 5km run"
   }
   ```
5. **Expected result**:
   - Status: 201 Created
   - Response contains progress record

6. Get progress history: **GET /api/goals/{id}/progress/history?page=0&size=10**
   - **Expected**: Shows paginated progress records

### Scenario 10: Frontend Integration Test

**Objective**: Verify React components can consume API responses

1. Open Frontend: http://localhost:5173
2. Login via Auth0
3. **Verify Dashboard** loads:
   - Should show profile card, recent weights, active goals, insights
4. **Navigate to Trends page**:
   - Should display weight history in chart/table
   - Verify pagination works if many entries
5. **Navigate to Goals page**:
   - Should list active goals
   - Should show progress tracking UI
6. **Open Check-In page**:
   - Should allow recording new weight
   - Should update dashboard after submission
7. **Check DevTools Console**:
   - No errors or warnings
   - API calls show proper response format

### Scenario 11: Cross-Endpoint Workflow

**Objective**: Verify complete user workflow

1. **Create health profile**:
   - POST /api/profile with height, activity level
   - Note the wellness_score

2. **Add weight entry**:
   - POST /api/weight with measured_at
   - Verify BMI calculates correctly

3. **Check updated profile**:
   - GET /api/profile
   - Verify wellness_score changed
   - Verify bmi_value matches calculation

4. **Create weight loss goal**:
   - POST /api/goals with target_weight_kg

5. **Record progress**:
   - POST /api/goals/{id}/progress/record
   - Multiple times to build history

6. **View goal progress**:
   - GET /api/goals/{id}/progress/history
   - Should show multiple records with pagination

### Scenario 12: Error Handling Test

**Objective**: Verify proper error responses

1. **Test 404 - Resource not found**:
   - GET /api/weight/00000000-0000-0000-0000-000000000000
   - **Expected**: Status 404, error message in response

2. **Test 400 - Invalid input**:
   - POST /api/weight with `weight_kg: "invalid"`
   - **Expected**: Status 400, validation error details

3. **Test 401 - No auth**:
   - POST /api/weight without Authorization header
   - **Expected**: Status 401 Unauthorized

4. **Test 403 - Ownership violation** (if possible):
   - DELETE /api/weight/{other_user_id}
   - **Expected**: Status 404 or 403

## Performance Tests

### Test Large Dataset

1. Create 100+ weight entries (can use script):

   ```bash
   for i in {1..100}; do
     curl -X POST http://localhost:8080/api/weight \
       -H "Content-Type: application/json" \
       -H "Authorization: Bearer $TOKEN" \
       -d "{\"weight_kg\": $((70 + RANDOM % 10)), \"measured_at\": \"2026-02-0$((i % 28 + 1))T10:00:00+02:00\", \"note\": \"Entry $i\"}"
   done
   ```

2. Test pagination performance:
   - GET /api/weight/history?page=0&size=100
   - **Expected**: Response time < 200ms

3. Monitor database:
   - Check soft delete query includes `deleted_at IS NULL` filter
   - Verify no N+1 queries

## Database Verification

### Connect to Database

```bash
psql -h localhost -U ndl -d ndl
```

### Verify Soft Delete Columns Exist

```sql
\d weight_entries
\d goals
\d health_profiles
\d ai_insights
```

### Check Soft Delete Status

```sql
-- All weight entries (including soft-deleted)
SELECT id, weight_kg, deleted_at FROM weight_entries;

-- Only active entries (as API returns)
SELECT id, weight_kg, deleted_at FROM weight_entries WHERE deleted_at IS NULL;

-- Count soft-deleted records
SELECT COUNT(*) FROM weight_entries WHERE deleted_at IS NOT NULL;
```

## Success Criteria

- [ ] All 12 scenarios pass without errors
- [ ] No console errors in browser DevTools
- [ ] Pagination returns correct totals
- [ ] Soft-deleted records don't appear in API responses
- [ ] Deleted records have `deleted_at IS NOT NULL` in database
- [ ] Ownership validation prevents cross-user access
- [ ] All HTTP status codes match expectations (201 for create, 204 for delete, etc.)
- [ ] Response DTOs have all expected fields with correct formatting
- [ ] Frontend can login, create entries, view data without errors
- [ ] Performance acceptable for 100+ records

## Troubleshooting

### Issue: 401 Unauthorized on all requests

**Solution**:

- Ensure backend is running
- Check Auth0 configuration in `application.yaml`
- Verify JWT token is valid via jwt.io

### Issue: Soft-deleted records still appearing

**Solution**:

- Verify `@SQLRestriction` annotation on entity
- Restart Spring Boot (rebuild cache)
- Check database directly: `SELECT * FROM weight_entries WHERE deleted_at IS NOT NULL`

### Issue: Pagination returns empty

**Solution**:

- Create test data first: POST /api/weight multiple times
- Check sort order matches query: `ORDER BY measured_at DESC`
- Verify page index starts at 0

### Issue: Frontend can't connect to backend

**Solution**:

- Verify backend running: `curl http://localhost:8080/api/ping`
- Check CORS configuration in backend
- Verify API_BASE_URL in frontend config matches backend

### Issue: BMI not recalculating after weight update

**Solution**:

- Verify health profile exists for user (POST /api/profile first)
- Check wellness score service is being called
- Verify height was set in profile (needed for BMI calc)

## Cleanup

After testing, to stop services:

```bash
# Stop frontend
Ctrl+C in frontend terminal

# Stop backend
Ctrl+C in backend terminal

# Stop database
cd infra && docker-compose down

# Clean up containers
docker-compose down -v
```

---

**Last Updated**: February 2, 2026  
**Status**: Manual Testing Phase
