#!/usr/bin/env bash
# Run from your Mac: deploy latest code to Ubuntu VM over SSH.
#
# Usage:
#   export SSH_HOST=203.0.113.50          # your VM IP
#   export SSH_USER=root                  # optional, default root
#   export REMOTE_DIR=/opt/mahir-backend  # optional
#   ./scripts/deploy-remote.sh
#
# First-time on the VM: clone repo, create .env.production (see docs/DEPLOY_SSH_VM.md),
# start Postgres: docker compose up -d

set -euo pipefail

SSH_USER="${SSH_USER:-root}"
SSH_HOST="${SSH_HOST:?Set SSH_HOST to your VM IP, e.g. export SSH_HOST=1.2.3.4}"
REMOTE_DIR="${REMOTE_DIR:-/opt/mahir-backend}"

echo "Deploying to ${SSH_USER}@${SSH_HOST}:${REMOTE_DIR}"

ssh "${SSH_USER}@${SSH_HOST}" bash -s -- "$REMOTE_DIR" <<'REMOTE'
set -euo pipefail
REMOTE_DIR="$1"
cd "$REMOTE_DIR"

if [ ! -f .env.production ]; then
  echo "ERROR: $REMOTE_DIR/.env.production missing on the server."
  echo "Create it with at least:"
  echo "  SPRING_PROFILES_ACTIVE=railway"
  echo "  DATABASE_URL=postgresql://mahir:YOUR_PASSWORD@postgres:5432/mahir_db"
  echo "  APP_JWT_SECRET=long-random-secret-at-least-32-chars"
  echo "Optional: APP_FIREBASE_SERVICE_ACCOUNT_JSON=... (single line JSON or use base64 var)"
  exit 1
fi

git pull origin main

# Optional: .env.postgres with POSTGRES_PASSWORD=... (must match DATABASE_URL password)
if [ -f .env.postgres ]; then
  docker compose --env-file .env.postgres up -d postgres
else
  docker compose up -d postgres
fi

docker build -t mahir-api .

docker rm -f mahir-api 2>/dev/null || true

NET=$(docker inspect mahir-postgres -f '{{range $k, $v := .NetworkSettings.Networks}}{{$k}}{{end}}')
echo "Docker network: $NET"

docker run -d --name mahir-api --restart unless-stopped \
  --network "$NET" \
  -p 8080:8080 \
  --env-file .env.production \
  mahir-api

echo "Done. Test: curl -s http://127.0.0.1:8080/health"
curl -s http://127.0.0.1:8080/health || true
echo
REMOTE

echo "From your Mac: curl http://${SSH_HOST}:8080/health"
