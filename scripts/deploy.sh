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

# ECS: set ECS_CLUSTER and ECS_SERVICE (and AWS_REGION) to deploy. The task definition is
# registered only if it does not exist yet; an existing one is left alone.
if [ -n "${ECS_CLUSTER:-}" ] && [ -n "${ECS_SERVICE:-}" ]; then
  scripts/ensure-task-definition.sh
  aws ecs update-service --cluster "$ECS_CLUSTER" --service "$ECS_SERVICE" \
    --force-new-deployment --region "${AWS_REGION:-ap-south-1}" >/dev/null
  echo "Deployment of '$ENV' started on $ECS_CLUSTER/$ECS_SERVICE."
  exit 0
fi

echo "Deployment to '$ENV' is not configured yet (set ECS_CLUSTER and ECS_SERVICE). See deployment/README.md." >&2
exit 1
