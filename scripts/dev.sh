#!/usr/bin/env bash
# Start the full local stack: Postgres (docker compose), backend, frontend, and ai-service.
# Ctrl+C stops everything this script started (Postgres keeps running; `docker compose down` to stop it).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

docker compose -f "$ROOT/docker-compose.yml" up -d --wait postgres

pids=()
trap 'kill "${pids[@]}" 2>/dev/null || true' EXIT INT TERM

(cd "$ROOT/backend" && mvn spring-boot:run) & pids+=($!)

(cd "$ROOT/frontend" && { [ -d node_modules ] || npm ci; } && npm run dev -- --host) & pids+=($!)

if [ -f "$ROOT/ai-service/.venv/bin/uvicorn" ]; then
  (cd "$ROOT/ai-service" && .venv/bin/uvicorn app.main:app --reload --port 8000) & pids+=($!)
else
  echo "ai-service: no .venv found, skipping (see ai-service/README.md)"
fi

echo "frontend  http://localhost:5173"
echo "backend   http://localhost:8081/api"
echo "ai        http://localhost:8000/health"
wait
