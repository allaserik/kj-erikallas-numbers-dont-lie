## Start local docker instance

```bash
docker compose -f ./infra/docker-compose.yml --env-file ./.env up -d
```

## Stop local docker instance

```bash
docker compose -f ./infra/docker-compose.yml --env-file ./.env down
```

## If you have trouble running docker commands

- Make sure your user is in docker group

```bash
groups
```

- Add your user into docker group if not

```bash
sudo usermod -aG docker $USER
```
## Checking the database tables through docker

- List all the tables

```bash
source .env
docker exec -it ndl_postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "\dt"
```

- Count entry rows of a table

```bash
docker exec -it ndl_postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "select count(*) from audit_events;"
```