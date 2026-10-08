# ECS

ECS task definitions and service definitions per component (backend, frontend, ai-service).

## task-definition.json

`task-definition.json` is a **fixed file kept in git**. Nothing generates or rewrites it on deploy.

`scripts/ensure-task-definition.sh` (called by `scripts/deploy.sh`) registers it in ECS **only if the
family does not exist yet**. If the family already exists it does nothing, so a deploy never creates a
new revision by itself. To publish a changed file on purpose, run it with `FORCE_REGISTER=true`.

The images use fixed tags (`...:backend`), so a deploy restarts the service with
`aws ecs update-service --force-new-deployment` and pulls the new image; it does not need a new revision.
