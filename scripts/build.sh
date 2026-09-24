#!/usr/bin/env bash
# Build and test every component. Mirrors what CI runs.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

echo "==> backend"
(cd "$ROOT/backend" && mvn -B verify)

echo "==> frontend"
(cd "$ROOT/frontend" && npm ci && { npm run lint || echo "WARN: frontend lint failed (non-blocking, as in CI)"; } && npm test && npm run build)

echo "==> ai-service"
(cd "$ROOT/ai-service" && python3 -m pip install -q -r requirements.txt && python3 -m pytest -q)
