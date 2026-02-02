# Soft Delete Quick Reference

## What Changed

### Database Level

- Added `deleted_at TIMESTAMP WITH TIME ZONE NULL` to 6 tables
- Migration: `V10__add_soft_delete.sql`

### JPA Entity Layer

- Replaced `@Where(clause = "...")` with `@SQLRestriction("...")` (Hibernate 6)
- Added `@SQLDelete(sql = "UPDATE ... SET deleted_at = CURRENT_TIMESTAMP WHERE ...")`
- Entities affected: User, Goal, GoalProgress, HealthProfile, WeightEntry, AiInsight

### Application Behavior

- DELETE requests now soft-delete (set `deleted_at` timestamp) instead of removing records
- SELECT queries automatically exclude soft-deleted records (via `@SQLRestriction`)
- No code changes needed in controllers, services, or frontend

## Testing Soft Deletes

### Prerequisites

```bash
# Terminal 1: Database
cd infra && docker-compose up

# Terminal 2: Backend
cd backend && ./mvnw spring-boot:run

# Terminal 3: Frontend
cd frontend && npm run dev
```

### Step-by-Step Test

1. **Create a goal:**

   ```bash
   TOKEN="eyJ..." # Get from browser localStorage
   curl -X POST http://localhost:8080/api/goals \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"targetValue": 75, "goalType": "WEIGHT_LOSS", "targetDate": "2025-12-31"}'
   # Returns: {"id": "abc123", ...}
   ```

2. **Verify it exists:**

   ```bash
   curl http://localhost:8080/api/goals -H "Authorization: Bearer $TOKEN"
   # Returns: [{"id": "abc123", ...}]
   ```

3. **Delete the goal:**

   ```bash
   curl -X DELETE http://localhost:8080/api/goals/abc123 \
     -H "Authorization: Bearer $TOKEN"
   # Returns: 204 No Content
   ```

4. **Verify soft delete in API:**

   ```bash
   curl http://localhost:8080/api/goals -H "Authorization: Bearer $TOKEN"
   # Returns: [] (empty - record is hidden)
   ```

5. **Verify soft delete in database:**

   ```bash
   psql -h localhost -U ndl -d ndl -c \
     "SELECT id, deleted_at FROM goals WHERE id = 'abc123';"
   # Returns: abc123 | 2025-01-29 00:57:37.123456+02
   ```

6. **Recovery (manual - if needed):**

   ```bash
   psql -h localhost -U ndl -d ndl -c \
     "UPDATE goals SET deleted_at = NULL WHERE id = 'abc123';"

   curl http://localhost:8080/api/goals -H "Authorization: Bearer $TOKEN"
   # Returns: [{"id": "abc123", ...}] (recovered!)
   ```

## Important Notes

### For Developers

1. Soft-deleted records are **automatically filtered** by Hibernate
2. Don't need to add `WHERE deleted_at IS NULL` to custom queries
3. `Repository.findById()`, `findAll()`, etc. work normally (excludes deleted)
4. If you need to find deleted records: use native queries with `nativeQuery=true`

### For Debugging

- Check Spring Boot logs: Look for SQL queries with `deleted_at IS NULL` filters
- Database inspection: Always check `deleted_at` IS NULL/NOT NULL
- Swagger UI: Endpoints work same as before, no API contract changes

### Performance Considerations

- Soft-deleted records use disk space (minor impact)
- Indexes on `deleted_at` can help if many deleted records exist
- Current filtering has negligible query overhead

## Rollback (if needed)

If soft deletes need to be removed:

1. **Drop migration:**

   ```bash
   # Remove V10__add_soft_delete.sql
   # Flyway will need database reset (drop + recreate)
   ```

2. **Remove entity annotations:**

   ```bash
   # Revert @SQLDelete and @SQLRestriction
   # Remove deletedAt field
   ```

3. **Restore old @Where (Hibernate 5):**
   ```java
   // If upgrading Spring Boot < 4.0
   import org.hibernate.annotations.Where;
   @Where(clause = "deleted_at IS NULL")
   ```

## Files Changed

- ✅ `V10__add_soft_delete.sql` - Database migration
- ✅ `UserEntity.java` - Soft delete support
- ✅ `GoalEntity.java` - Soft delete support
- ✅ `GoalProgressEntity.java` - Soft delete support
- ✅ `HealthProfileEntity.java` - Soft delete support
- ✅ `WeightEntryEntity.java` - Soft delete support
- ✅ `AiInsightEntity.java` - Soft delete support

## Build Status

```
✅ Backend: BUILD SUCCESS (82 files compiled)
✅ Frontend: Build successful (395 modules, 10.09s)
```
