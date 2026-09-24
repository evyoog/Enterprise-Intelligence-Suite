# Deployment

| Folder | Contents |
|--------|----------|
| `docker/` | Dockerfile template and the Postgres init script used by the root `docker-compose.yml` |
| `aws/` | AWS pipeline templates (PaaS and SaaS tiers) |
| `ecs/` | ECS task and service definitions |
| `nginx/` | Reverse proxy / SPA hosting config |
| `environments/{dev,uat,prod}/` | Non-secret per-environment configuration |

Secrets are never committed. They come from the runtime environment or AWS Secrets Manager.
