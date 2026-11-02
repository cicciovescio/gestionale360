#!/bin/sh
# Deploy con Docker puro: Postgres + API (seed demo) + frontend.
set -e
cd "$(dirname "$0")"

NET=meridiana
VOL=meridiana_pgdata
DB_NAME="${POSTGRES_DB:-meridiana}"
DB_USER="${POSTGRES_USER:-meridiana}"
DB_PASS="${POSTGRES_PASSWORD:-meridiana}"
JWT="${MERIDIANA_JWT_SECRET:-meridiana-local-dev-secret-change-me-32chars-min!!}"
PORT="${APP_PORT:-80}"

if [ -f .env ]; then
  set -a
  # shellcheck disable=SC1091
  . ./.env
  set +a
  DB_NAME="${POSTGRES_DB:-$DB_NAME}"
  DB_USER="${POSTGRES_USER:-$DB_USER}"
  DB_PASS="${POSTGRES_PASSWORD:-$DB_PASS}"
  JWT="${MERIDIANA_JWT_SECRET:-$JWT}"
  PORT="${APP_PORT:-$PORT}"
fi

echo "Rete e volume..."
docker network inspect "$NET" >/dev/null 2>&1 || docker network create "$NET"
docker volume inspect "$VOL" >/dev/null 2>&1 || docker volume create "$VOL"

echo "PostgreSQL..."
if docker inspect postgres >/dev/null 2>&1; then
  docker rm -f postgres >/dev/null
fi
docker run -d --name postgres --network "$NET" --restart unless-stopped \
  -e POSTGRES_DB="$DB_NAME" \
  -e POSTGRES_USER="$DB_USER" \
  -e POSTGRES_PASSWORD="$DB_PASS" \
  -v "$VOL":/var/lib/postgresql/data \
  postgres:16-alpine

echo "Attendo Postgres..."
i=0
until docker exec postgres pg_isready -U "$DB_USER" -d "$DB_NAME" >/dev/null 2>&1; do
  i=$((i + 1))
  if [ "$i" -gt 40 ]; then
    echo "Postgres non risponde."
    docker logs postgres
    exit 1
  fi
  sleep 2
done

echo "Build API..."
docker build -t meridiana-api ./backend
if docker inspect api >/dev/null 2>&1; then
  docker rm -f api >/dev/null
fi
docker run -d --name api --network "$NET" --restart unless-stopped \
  -e SPRING_PROFILES_ACTIVE=postgres \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://postgres:5432/${DB_NAME}" \
  -e SPRING_DATASOURCE_USERNAME="$DB_USER" \
  -e SPRING_DATASOURCE_PASSWORD="$DB_PASS" \
  -e MERIDIANA_JWT_SECRET="$JWT" \
  meridiana-api

echo "Attendo API e inserimento dati demo..."
i=0
while [ "$i" -lt 90 ]; do
  logs=$(docker logs api 2>&1 || true)
  echo "$logs" | grep -q "Started MeridianaApplication" && break
  echo "$logs" | grep -qi "APPLICATION FAILED TO START" && {
    echo "API non è partita:"
    docker logs api
    exit 1
  }
  i=$((i + 1))
  sleep 2
done
if ! docker logs api 2>&1 | grep -q "Started MeridianaApplication"; then
  echo "Timeout avvio API:"
  docker logs api
  exit 1
fi

echo "Build frontend..."
docker build -t meridiana-web ./frontend
if docker inspect web >/dev/null 2>&1; then
  docker rm -f web >/dev/null
fi
docker run -d --name web --network "$NET" --restart unless-stopped \
  -p "${PORT}:80" \
  meridiana-web

echo
docker ps
echo
echo "Pronto su http://IP-DEL-SERVER:${PORT}"
echo "Login demo: bruno@mposta.it / Bruno2006@"
echo "Altri: anna@meridiana.local / Anna2026@   luca@meridiana.local / Luca2026@"
