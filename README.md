# Wellness App

kood/Jõhvi generative AI specialization Wellness App project

- KJ gitea's:

[numbers-dont-lie](https://gitea.kood.tech/erikallas/numbers-dont-lie.git)

[ai-assistant](https://gitea.kood.tech/erikallas/ai-assistant.git)

[counting-calories](https://gitea.kood.tech/erikallas/counting-calories.git)


## Start local docker instance (postgres)

```bash
docker compose -f ./infra/docker-compose.yml --env-file ./.env up -d
```

## Stop local docker instance

```bash
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
