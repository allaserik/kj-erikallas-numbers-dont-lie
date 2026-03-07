# Wellness App

kood/Jõhvi generative AI specialization Wellness App project

- KJ gitea's:

[numbers-dont-lie](https://gitea.kood.tech/erikallas/numbers-dont-lie.git)

[ai-assistant](https://gitea.kood.tech/erikallas/ai-assistant.git)

[counting-calories](https://gitea.kood.tech/erikallas/counting-calories.git)

## Start entire app in Docker (one command!)

**Production (no demo data):**

```bash
docker-compose up -d
```

**With Demo Data:**

```bash
DEMO_MODE=true VITE_DEMO_MODE=true docker-compose up -d
# Or using env file:
source .env.docker.demo && docker-compose up -d
```

Then visit: http://localhost:5173

**Stop everything:**

```bash
docker-compose down
```

## Local Development (Frontend + Backend local, Database in Docker)

Start the database:

```bash
docker compose -f ./infra/docker-compose.yml --env-file ./.env up -d
```

With demo data:

```bash
source .env.demo && docker compose -f ./infra/docker-compose.yml --env-file ./.env up -d
```

## Legacy: Start just database (for local development)

```bash
docker compose -f ./infra/docker-compose.yml --env-file ./.env up -d
docker compose -f ./infra/docker-compose.yml --env-file ./.env down
```

Run the backend

```bash
cd backend
./mvnw -U clean test
./mvnw spring-boot:run

./mvnw clean test 2>&1 | grep -A 10 "Caused by"

./mvnw clean compile -DskipTests 2>&1 | tail -60
```

Run the frontend

```bash
cd frontend
npm install
npm run dev
```

install ollama
[host ALL your AI locally](https://www.youtube.com/watch?v=Wjrdr0NU4Sk)

OpenAPI Swagger documentation
[OpenAPI Swagger URL](http://localhost:8080/swagger-ui/index.html)
