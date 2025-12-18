

Create `.env` file
```env
POSTGRES_USER=appuser
POSTGRES_PASSWORD=apppassword
POSTGRES_DB=appdb

```

```bash
docker volume create pg_data
docker compose up
```