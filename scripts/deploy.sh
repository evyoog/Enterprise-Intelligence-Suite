#!/usr/bin/env bash
# Deploy to an environment: scripts/deploy.sh <dev|uat|prod>
#
# Placeholder — the target (ECR/ECS on AWS) is not wired up yet. Pipeline templates
# live in deployment/aws/, per-environment config in deployment/environments/<env>/.
# Credentials must come from the environment (e.g. AWS_PROFILE / OIDC), never this repo.
set -euo pipefail

ENV="${1:-}"
case "$ENV" in
  dev|uat|prod) ;;
  *) echo "usage: $0 <dev|uat|prod>" >&2; exit 2 ;;
esac

echo "Deployment to '$ENV' is not configured yet. See deployment/README.md." >&2
exit 1
