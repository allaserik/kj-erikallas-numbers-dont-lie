# Infra

## Start Postgres

From repo root:

cp .env.example .env
docker compose -f infra/docker-compose.yml --env-file .env up -d

## Stop

docker compose -f infra/docker-compose.yml --env-file .env down
