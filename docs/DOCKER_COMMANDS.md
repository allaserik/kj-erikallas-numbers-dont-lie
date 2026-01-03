Start local docker instance

```shell
docker compose -f ./infra/docker-compose.yml --env-file ./.env up -d
```

Stop local docker instance

```shell
docker compose -f ./infra/docker-compose.yml --env-file ./.env down
```

If you have trouble running docker commands

- Make sure your user is in docker group

```shell
groups
```

- Add your user into docker group if not

```shell
sudo usermod -aG docker $USER
```