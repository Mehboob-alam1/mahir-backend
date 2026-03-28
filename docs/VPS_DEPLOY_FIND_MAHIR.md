# Ubuntu VPS – Find Mahir backend (Spring Boot + Docker + Postgres)

Replace **YOUR_SERVER_IP** and **YOUR_GITHUB_USER** (or full clone URL) with your values.

---

## 1) Log in

```bash
ssh root@YOUR_SERVER_IP
```

---

## 2) Update server

```bash
apt update && apt upgrade -y
```

---

## 3) Install Docker + Git

```bash
apt install -y docker.io docker-compose-v2 git
systemctl enable docker --now
docker --version
```

---

## 4) Firewall (ufw)

Allow SSH first, then the app port.

```bash
ufw allow 22/tcp
ufw allow 8080/tcp
ufw enable
ufw status
```

Also open **8080** in your cloud provider’s firewall (DigitalOcean, Hetzner, etc.).

---

## 5) Clone repo

```bash
mkdir -p /opt
cd /opt
git clone https://github.com/Mehboob-alam1/mahir-backend.git
cd mahir-backend
```

If your repo folder name differs, `cd` into the folder that contains `Dockerfile` and `pom.xml`.

---

## 6) Postgres (Docker Compose example)

Create `docker-compose.yml` next to the repo (or in repo) if you don’t have one:

```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: mahir-postgres
    restart: unless-stopped
    environment:
      POSTGRES_USER: mahir
      POSTGRES_PASSWORD: CHANGE_ME_STRONG_PASSWORD
      POSTGRES_DB: mahir_db
    volumes:
      - mahir_pgdata:/var/lib/postgresql/data
    ports:
      - "127.0.0.1:5432:5432"

volumes:
  mahir_pgdata:
```

Start it:

```bash
cd /opt/mahir-backend
docker compose up -d postgres
```

**DATABASE_URL** for the app (same user/pass/db as above):

```text
postgresql://mahir:CHANGE_ME_STRONG_PASSWORD@postgres:5432/mahir_db
```

(Use `@postgres` as host when the API container is on the **same Docker network** as this Postgres service.)

---

## 7) Build API image

```bash
cd /opt/mahir-backend
docker build -t mahir-api .
```

---

## 8) Docker network name

```bash
docker ps
docker inspect mahir-postgres -f '{{range $k, $v := .NetworkSettings.Networks}}{{$k}}{{end}}'
```

Example output: `mahir-backend_default`. Save it as **NET** below.

---

## 9) Run API container

Generate a long JWT secret (on the server):

```bash
openssl rand -base64 48
```

Run (replace **NET**, **JWT_SECRET**, and **DATABASE_URL** password if you changed them):

```bash
docker rm -f mahir-api 2>/dev/null

NET=$(docker inspect mahir-postgres -f '{{range $k, $v := .NetworkSettings.Networks}}{{$k}}{{end}}')
echo "$NET"

docker run -d --name mahir-api --restart unless-stopped \
  --network "$NET" \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=railway \
  -e DATABASE_URL=postgresql://mahir:CHANGE_ME_STRONG_PASSWORD@postgres:5432/mahir_db \
  -e APP_JWT_SECRET=PASTE_YOUR_OPENSSL_OUTPUT_HERE \
  -e APP_FIREBASE_SERVICE_ACCOUNT_JSON='{"type":"service_account",...}' \
  mahir-api
```

**Notes:**

- **APP_FIREBASE_SERVICE_ACCOUNT_JSON**: paste the **full** Firebase JSON on one line, or omit this line if you don’t need push (in-app notifications still work).
- If multiline JSON breaks the shell, use a single-line base64 variable instead: set **APP_FIREBASE_SERVICE_ACCOUNT_JSON_BASE64** (see `docs/FIREBASE_PUSH_SETUP.md`).

---

## 10) Test on the server

```bash
curl -s http://127.0.0.1:8080/health
```

Expected: JSON with `"status":"UP"` and `"service":"mahir-backend"`.

```bash
docker logs mahir-api
```

---

## 11) Test from your Mac

```bash
curl http://YOUR_SERVER_IP:8080/health
```

If this fails but step 10 works: fix **ufw** and the **cloud firewall** for port **8080**.

---

## 12) After `git push` (manual redeploy)

```bash
ssh root@YOUR_SERVER_IP
cd /opt/mahir-backend
git pull origin main
docker build -t mahir-api .
docker rm -f mahir-api
NET=$(docker inspect mahir-postgres -f '{{range $k, $v := .NetworkSettings.Networks}}{{$k}}{{end}}')
docker run -d --name mahir-api --restart unless-stopped \
  --network "$NET" \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=railway \
  -e DATABASE_URL=postgresql://mahir:CHANGE_ME_STRONG_PASSWORD@postgres:5432/mahir_db \
  -e APP_JWT_SECRET=PASTE_YOUR_OPENSSL_OUTPUT_HERE \
  mahir-api
```

(Add **APP_FIREBASE_SERVICE_ACCOUNT_JSON** again if you use FCM.)

---

## 13) Find Mahir API (auth) – not Marvron paths

| Action | Method | Path |
|--------|--------|------|
| Health | GET | `/health` |
| Sign up | POST | `/auth/signup` |
| Sign in | POST | `/auth/signin` |
| Refresh | POST | `/auth/refresh` |

**Base URL:** `http://YOUR_SERVER_IP:8080`

**Protected routes:** header `Authorization: Bearer <accessToken>` (from sign-in response).

Postman: set **baseUrl** to `http://YOUR_SERVER_IP:8080` (collection uses `{{baseUrl}}`).

---

## Quick checklist

- [ ] SSH works  
- [ ] Docker + git installed  
- [ ] ufw: 22 + 8080; cloud firewall 8080  
- [ ] Repo cloned under `/opt`  
- [ ] Postgres running; **DATABASE_URL** uses host `postgres` on same Docker network  
- [ ] Image built: `mahir-api`  
- [ ] **SPRING_PROFILES_ACTIVE=railway**, **APP_JWT_SECRET** set  
- [ ] `curl` health OK on server and from Mac  
