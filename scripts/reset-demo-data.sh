#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SQL_FILE="$ROOT_DIR/scripts/reset-demo-data.sql"

if [[ -f "$ROOT_DIR/.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "$ROOT_DIR/.env"
  set +a
fi

POSTGRES_DB="${POSTGRES_DB:-plazavea_perecibles}"
POSTGRES_USER="${POSTGRES_USER:-plazavea}"
POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-changeme}"
POSTGRES_HOST="${POSTGRES_HOST:-localhost}"
POSTGRES_PORT="${POSTGRES_PORT:-5432}"

if [[ -n "${DB_URL:-}" && "$DB_URL" =~ ^jdbc:postgresql://([^:/?#]+)(:([0-9]+))?/([^?]+) ]]; then
  POSTGRES_HOST="${BASH_REMATCH[1]}"
  POSTGRES_PORT="${BASH_REMATCH[3]:-5432}"
  POSTGRES_DB="${BASH_REMATCH[4]}"
fi

echo "Resetting demo data in database '$POSTGRES_DB' at '$POSTGRES_HOST:$POSTGRES_PORT' while preserving users..."

if command -v psql >/dev/null 2>&1; then
  PGPASSWORD="$POSTGRES_PASSWORD" psql \
    -h "$POSTGRES_HOST" \
    -p "$POSTGRES_PORT" \
    -U "$POSTGRES_USER" \
    -d "$POSTGRES_DB" \
    -v ON_ERROR_STOP=1 \
    -f "$SQL_FILE"
elif command -v docker >/dev/null 2>&1; then
  docker run --rm -i \
    -e PGPASSWORD="$POSTGRES_PASSWORD" \
    postgres:16 \
    psql \
    -h "$POSTGRES_HOST" \
    -p "$POSTGRES_PORT" \
    -U "$POSTGRES_USER" \
    -d "$POSTGRES_DB" \
    -v ON_ERROR_STOP=1 < "$SQL_FILE"
else
  echo "Neither psql nor docker is available. Install PostgreSQL client tools or Docker to run this script." >&2
  exit 1
fi

echo "Demo data reset complete."
