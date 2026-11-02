#!/bin/sh
set -e
HOST="${POSTGRES_HOST:-postgres}"
echo "Attendo PostgreSQL su ${HOST}:5432..."
i=0
while [ "$i" -lt 60 ]; do
  if nc -z "$HOST" 5432; then
    echo "PostgreSQL pronto. Avvio API e seed demo..."
    exec java -jar /app/app.jar
  fi
  i=$((i + 1))
  sleep 2
done
echo "Timeout: PostgreSQL non raggiungibile su ${HOST}:5432"
exit 1
