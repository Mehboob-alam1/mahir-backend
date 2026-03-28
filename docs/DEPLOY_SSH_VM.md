# Deploy this backend to a VM over SSH

## A) Connect with SSH (from Mac Terminal)

```bash
ssh root@YOUR_VM_IP
```

**VS Code / Cursor:** Install the **Remote - SSH** extension → Command Palette → **Remote-SSH: Connect to Host** → add `root@YOUR_VM_IP` → open the `/opt/mahir-backend` folder on the server to edit files there.

---

## B) One-time VM setup

On the server (after SSH):

```bash
apt update && apt upgrade -y
apt install -y docker.io docker-compose-v2 git
systemctl enable docker --now

ufw allow 22/tcp
ufw allow 8080/tcp
ufw enable
```

Clone the repo:

```bash
mkdir -p /opt && cd /opt
git clone https://github.com/Mehboob-alam1/mahir-backend.git
cd mahir-backend
```

Create **secrets file** (never commit this):

```bash
nano .env.production
```

Put (adjust password and JWT secret):

```env
SPRING_PROFILES_ACTIVE=railway
DATABASE_URL=postgresql://mahir:YOUR_STRONG_PASSWORD@postgres:5432/mahir_db
APP_JWT_SECRET=paste_output_of_openssl_rand_base64_48
```

Generate JWT secret:

```bash
openssl rand -base64 48
```

Optional – Firebase push (one long line JSON, or skip):

```env
APP_FIREBASE_SERVICE_ACCOUNT_JSON={"type":"service_account",...}
```

Set the **same** DB password for Compose. Either:

```bash
echo 'POSTGRES_PASSWORD=YOUR_STRONG_PASSWORD' > .env.postgres
```

or edit `docker-compose.yml` defaults (not recommended).

Start Postgres:

```bash
docker compose --env-file .env.postgres up -d postgres
```

First deploy (build + run):

```bash
docker build -t mahir-api .
NET=$(docker inspect mahir-postgres -f '{{range $k, $v := .NetworkSettings.Networks}}{{$k}}{{end}}')
docker run -d --name mahir-api --restart unless-stopped \
  --network "$NET" -p 8080:8080 \
  --env-file .env.production \
  mahir-api
```

Test:

```bash
curl http://127.0.0.1:8080/health
```

Open port **8080** in your cloud provider firewall if needed.

---

## C) Deploy updates from your Mac (script)

In the repo on your Mac:

```bash
chmod +x scripts/deploy-remote.sh
export SSH_HOST=YOUR_VM_IP
./scripts/deploy-remote.sh
```

The script SSHs in, `git pull`, rebuilds the image, restarts `mahir-api` with `.env.production`.

Requires on the server: `/opt/mahir-backend/.env.production` (and `.env.postgres` if you use it for `POSTGRES_PASSWORD`).

---

## D) Manual update on the VM

```bash
ssh root@YOUR_VM_IP
cd /opt/mahir-backend
git pull origin main
docker compose --env-file .env.postgres up -d postgres
docker build -t mahir-api .
docker rm -f mahir-api
NET=$(docker inspect mahir-postgres -f '{{range $k, $v := .NetworkSettings.Networks}}{{$k}}{{end}}')
docker run -d --name mahir-api --restart unless-stopped \
  --network "$NET" -p 8080:8080 \
  --env-file .env.production \
  mahir-api
```

---

Full reference: `docs/VPS_DEPLOY_FIND_MAHIR.md`.
