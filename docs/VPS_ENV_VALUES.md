# VPS environment values (match `docker-compose.yml` defaults)

Use these **literal** strings so Postgres and the API agree. They match  
`${POSTGRES_PASSWORD:-changeme}` when you do **not** export `POSTGRES_PASSWORD`.

| Name | Value |
|------|--------|
| `POSTGRES_USER` | `mahir` |
| `POSTGRES_PASSWORD` | `changeme` |
| `POSTGRES_DB` | `mahir_db` |
| Postgres hostname **from inside** another container on the same Compose network | `postgres` |
| **`DATABASE_URL` for `SPRING_PROFILES_ACTIVE=railway`** | `postgresql://mahir:changeme@postgres:5432/mahir_db` |

**Security:** change `changeme` (and rotate `APP_JWT_SECRET`) once everything works.  
If you already created the Postgres **volume** with a different password, either use that old password in `DATABASE_URL` or remove the volume and run `docker compose up -d postgres` again (this **wipes** the database).

**`APP_JWT_SECRET`:** must be long and random. Generate on the server:

```bash
openssl rand -base64 48
```

**`APP_RESET_PASSWORD_BASE_URL`:** public URL users open for reset links, e.g. `https://api.yourdomain.com` or `http://YOUR_PUBLIC_IP:8080` while testing.

See `docs/VPS_DEPLOY_FIND_MAHIR.md` for full deploy steps.
