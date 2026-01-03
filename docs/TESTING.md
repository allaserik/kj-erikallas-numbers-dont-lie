# How to test this App?

## Test endpoints

In another terminal:

```bash
curl -s http://localhost:8080/api/ping
```

Expected:

```json
{ "status": "ok" }
```

If you have Actuator on the classpath, also:

```bash
curl -s http://localhost:8080/actuator/health
```

curl -s -X POST http://localhost:8080/api/audit-test


## Test run (unit tests)

From `backend/`:

```bash
./mvnw test
```
