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

Local Development (Frontend + Backend locally, Database in Docker)

**Terminal 1 - Start database only:**

```bash
docker compose -f ./infra/docker-compose.yml --env-file ./.env up -d
```

**Terminal 2 - Start backend:**

```bash
cd backend
./mvnw -U clean test
./mvnw -q -DskipTests compile
DEMO_MODE=true ./mvnw spring-boot:run
```

**Terminal 3 - Start frontend:**

```bash
cd frontend
VITE_DEMO_MODE=true npm run dev
```

install ollama
[host ALL your AI locally](https://www.youtube.com/watch?v=Wjrdr0NU4Sk)

OpenAPI Swagger documentation
[OpenAPI Swagger URL](http://localhost:8080/swagger-ui/index.html)

## Run with openai api key

```bash
cd /home/erik/dev/kood/erikallas/numbers-dont-lie
set -a
source .env
set +a

cd backend
./mvnw spring-boot:run

echo $OPENAI_API_KEY | head -c 12 && echo "..."
```
