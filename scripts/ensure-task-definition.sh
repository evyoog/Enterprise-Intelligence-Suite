#!/usr/bin/env bash
# Register the ECS task definition only when it does not exist yet.
#
#   scripts/ensure-task-definition.sh [path/to/task-definition.json]
#
# - Family not found in ECS  -> register deployment/ecs/task-definition.json (creates revision 1).
# - Family already exists    -> do nothing (no new revision, the file is never rewritten).
# - FORCE_REGISTER=true      -> register a new revision on purpose (for example after editing the file).
#
# Credentials come from the environment (AWS_PROFILE / OIDC), never from this repo.
set -euo pipefail

FILE="${1:-deployment/ecs/task-definition.json}"
REGION="${AWS_REGION:-ap-south-1}"

[ -f "$FILE" ] || { echo "Task definition file not found: $FILE" >&2; exit 2; }
command -v jq >/dev/null || { echo "jq is required" >&2; exit 2; }
command -v aws >/dev/null || { echo "aws CLI is required" >&2; exit 2; }

FAMILY="$(jq -r '.family' "$FILE")"
[ -n "$FAMILY" ] && [ "$FAMILY" != "null" ] || { echo "No \"family\" in $FILE" >&2; exit 2; }

if [ "${FORCE_REGISTER:-false}" != "true" ] \
   && aws ecs describe-task-definition --task-definition "$FAMILY" --region "$REGION" >/dev/null 2>&1; then
  echo "Task definition '$FAMILY' already exists in $REGION - not creating a new one."
  exit 0
fi

echo "Registering task definition '$FAMILY' in $REGION from $FILE"
aws ecs register-task-definition --cli-input-json "file://$FILE" --region "$REGION" >/dev/null
echo "Task definition '$FAMILY' registered."
