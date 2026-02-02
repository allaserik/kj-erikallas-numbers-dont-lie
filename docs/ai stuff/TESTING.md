# How to test this App?

## Test endpoints

In another terminal:

### Ping

```bash
curl -s http://localhost:8080/api/ping
```

Expected:

```json
{ "status": "ok" }
```

### Actuator

If you have Actuator on the classpath, also:

```bash
curl -s http://localhost:8080/actuator/health
```

Expected:

```json
{ "groups": ["liveness", "readiness"], "status": "UP" }
```

### Audit Table entry Test

```bash
curl -s -X POST http://localhost:8080/api/audit-test
```

### Validation exception test

```bash
curl -s -X POST http://localhost:8080/api/validate-test \
  -H "Content-Type: application/json" \
  -d '{"name":"","email":"not-an-email"}' | jq .
```

Expected:

```json
{
  "timestamp": "2026-01-03T19:08:56.638544545+02:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/validate-test",
  "fieldErrors": [
    {
      "field": "email",
      "message": "email must be valid"
    },
    {
      "field": "name",
      "message": "name is required"
    }
  ]
}
```

## Test run (unit tests)

From `backend/`:

```bash
./mvnw test
```
